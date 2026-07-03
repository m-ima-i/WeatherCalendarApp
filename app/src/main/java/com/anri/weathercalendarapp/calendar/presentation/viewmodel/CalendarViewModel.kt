package com.anri.weathercalendarapp.calendar.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anri.weathercalendarapp.calendar.domain.model.EventColors
import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.model.request.CalendarReq
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.usecase.CreateCalendarEventUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.DeleteCalendarEventUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.GetCalendarUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.GetHolidaysUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.GetShowHolidaysUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.SyncCalendarEventsToLocalUseCase
import com.anri.weathercalendarapp.calendar.domain.usecase.UpdateCalendarEventUseCase
import com.anri.weathercalendarapp.calendar.presentation.state.CalendarUiState
import com.anri.weathercalendarapp.calendar.presentation.type.CalendarFailureType
import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.common.auth.GoogleAuthTokenProvider
import com.anri.weathercalendarapp.common.auth.TokenResult
import com.anri.weathercalendarapp.common.datetime.parseEventDate
import com.anri.weathercalendarapp.common.network.NetworkAvailabilityChecker
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val googleAuthTokenProvider: GoogleAuthTokenProvider,
    private val authPreferences: AuthPreferences,
    private val getCalendarUseCase: GetCalendarUseCase,
    private val getHolidaysUseCase: GetHolidaysUseCase,
    private val getShowHolidaysUseCase: GetShowHolidaysUseCase,
    private val createCalendarEventUseCase: CreateCalendarEventUseCase,
    private val updateCalendarEventUseCase: UpdateCalendarEventUseCase,
    private val deleteCalendarEventUseCase: DeleteCalendarEventUseCase,
    private val syncCalendarEventsToLocalUseCase: SyncCalendarEventsToLocalUseCase,
    private val globalUiManager: GlobalUiManager,
    private val networkAvailabilityChecker: NetworkAvailabilityChecker,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "CalendarViewModel"
    }

    private val _uiState = MutableStateFlow(CalendarUiState(eventColors = EventColors.PALETTE))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // ナビゲーションバー遷移時にカレンダー画面（Pager位置含む）を初期化するシグナル
    private val _resetUiSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val resetUiSignal: SharedFlow<Unit> = _resetUiSignal.asSharedFlow()

    // 読み込み済みの日付範囲を管理
    private var loadedRangeStart: YearMonth = YearMonth.now().minusMonths(12)
    private var loadedRangeEnd: YearMonth = YearMonth.now().plusMonths(12)

    // 祝日の読み込み済み範囲を管理
    private var holidayLoadedRangeStart: YearMonth = YearMonth.now().minusMonths(12)
    private var holidayLoadedRangeEnd: YearMonth = YearMonth.now().plusMonths(12)

    // プリフェッチ中フラグ（二重呼び出し防止）
    private var isPrefetchingFuture = false
    private var isPrefetchingPast = false

    init {
        viewModelScope.launch {
            getShowHolidaysUseCase().collect { showHolidays ->
                _uiState.update { it.copy(showHolidays = showHolidays) }
            }
        }
    }

    /**
     * 起動プロセス: カレンダーAPIプロセス
     * - accountEmail なし → isInitialized=true / isAuthorized=false / failureType=null（連携誘導UI）
     * - accountEmail あり → requestAccessToken() でトークン取得
     *   - NeedsConsent（連携解除/要同意）→ failureType=API_UNAUTHORIZED（再連携UI）
     *   - TransientFailure（一時的失敗）→ failureType=API_UNAUTHORIZED（既存挙動踏襲）
     *   - Success → カレンダーAPI（±12ヶ月分）。401 → トークン強制リフレッシュして1回リトライ
     */
    fun runCalendarProcess(
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val email = authPreferences.accountEmail.first()
            if (email == null) {
                _uiState.update {
                    it.copy(
                        isInitialized = true,
                        isAuthorized = false,
                        failureType = null
                    )
                }
                onComplete()
                return@launch
            }

            // 機内モード+WiFi未接続なら GMS 認証要求を待たず即ネットワークエラー判定
            if (networkAvailabilityChecker.isOfflineDueToAirplaneMode()) {
                applyRefreshableFailure(CalendarFailureType.API_NETWORK_ERROR, setInitialized = true)
                onComplete()
                return@launch
            }

            val token = when (val r = googleAuthTokenProvider.requestAccessToken(appContext)) {
                is TokenResult.Success -> r.token
                TokenResult.NeedsConsent -> {
                    applyAuthFailure(setInitialized = true)
                    onComplete()
                    return@launch
                }
                TokenResult.TransientFailure -> {
                    applyRefreshableFailure(CalendarFailureType.API_NETWORK_ERROR, setInitialized = true)
                    onComplete()
                    return@launch
                }
            }

            val rangeStart = YearMonth.now().minusMonths(12)
            val rangeEnd = YearMonth.now().plusMonths(12)
            val req = buildCalendarReq(rangeStart, rangeEnd)

            _uiState.update { it.copy(isLoading = true) }

            val firstFetch = fetchEventsAndHolidays(req, rangeStart, rangeEnd, token)

            // 401 ならトークン強制リフレッシュして1回リトライ
            val (eventsResult, holidaysResult) = if (
                firstFetch.first is CalendarFetchResult.Failure
                && (firstFetch.first as CalendarFetchResult.Failure).type == CalendarFailureType.API_UNAUTHORIZED
            ) {
                when (val r = googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = true)) {
                    is TokenResult.Success -> {
                        fetchEventsAndHolidays(req, rangeStart, rangeEnd, r.token)
                    }
                    TokenResult.NeedsConsent -> {
                        applyAuthFailure(setInitialized = true)
                        onComplete()
                        return@launch
                    }
                    TokenResult.TransientFailure -> {
                        applyRefreshableFailure(CalendarFailureType.API_NETWORK_ERROR, setInitialized = true)
                        onComplete()
                        return@launch
                    }
                }
            } else {
                firstFetch
            }

            handleApiResult(
                eventsResult = eventsResult,
                holidaysResult = holidaysResult,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
                setInitialized = true
            )
            onComplete()
        }
    }

    /**
     * 連携解除/再認証必須の失敗用。
     * @param setInitialized Process 経路では true（初期化完了とみなす）、ApiOnly では false（既存の isInitialized を維持）
     */
    private fun applyAuthFailure(setInitialized: Boolean) {
        googleAuthTokenProvider.clearCachedToken()
        _uiState.update {
            it.copy(
                isInitialized = if (setInitialized) true else it.isInitialized,
                isAuthorized = false,
                isLoading = false,
                failureType = CalendarFailureType.API_UNAUTHORIZED,
                overlayDate = null
            )
        }
    }

    /**
     * 連携解除ではない一時的失敗用（リフレッシュボタン表示）。
     * @param setInitialized Process 経路では true、ApiOnly では false
     */
    private fun applyRefreshableFailure(type: CalendarFailureType, setInitialized: Boolean) {
        _uiState.update {
            it.copy(
                isInitialized = if (setInitialized) true else it.isInitialized,
                isLoading = false,
                failureType = type,
                overlayDate = null
            )
        }
    }

    private suspend fun fetchEventsAndHolidays(
        req: CalendarReq,
        rangeStart: YearMonth,
        rangeEnd: YearMonth,
        token: String
    ): Pair<CalendarFetchResult, List<HolidayEvent>?> = coroutineScope {
        val eventsDeferred = async { fetchEventsResult(req, token) }
        val holidaysDeferred = async { fetchHolidaysResult(rangeStart, rangeEnd, token) }
        eventsDeferred.await() to holidaysDeferred.await()
    }

    /**
     * API 結果反映の共通処理 (Process / ApiOnly 共通)。
     * onComplete は呼び出し元（Process のみ必要）で行う。
     */
    private suspend fun handleApiResult(
        eventsResult: CalendarFetchResult,
        holidaysResult: List<HolidayEvent>?,
        rangeStart: YearMonth,
        rangeEnd: YearMonth,
        setInitialized: Boolean
    ) {
        when {
            eventsResult is CalendarFetchResult.Success && holidaysResult != null -> {
                // 天気APIと同じく Local 保存 + Widget refresh を UI state 更新より先に行う
                syncCalendarEventsToLocalUseCase(eventsResult.events)
                _uiState.update {
                    it.copy(
                        isInitialized = if (setInitialized) true else it.isInitialized,
                        isAuthorized = true,
                        isLoading = false,
                        events = eventsResult.events,
                        holidays = holidaysResult,
                        failureType = null
                    )
                }
                loadedRangeStart = rangeStart
                loadedRangeEnd = rangeEnd
                holidayLoadedRangeStart = rangeStart
                holidayLoadedRangeEnd = rangeEnd
            }
            eventsResult is CalendarFetchResult.Failure
                    && eventsResult.type == CalendarFailureType.API_UNAUTHORIZED -> {
                // リトライ後も 401 = 連携解除ではない特殊状態 → リフレッシュボタン表示
                applyRefreshableFailure(CalendarFailureType.API_UNKNOWN, setInitialized)
            }
            eventsResult is CalendarFetchResult.Failure -> {
                _uiState.update {
                    it.copy(
                        isInitialized = if (setInitialized) true else it.isInitialized,
                        isLoading = false,
                        failureType = eventsResult.type,
                        overlayDate = null
                    )
                }
            }
            else -> {
                // events Success だが holidays 取得失敗 → events のみ反映し祝日失敗を Toast で通知
                val events = (eventsResult as CalendarFetchResult.Success).events
                syncCalendarEventsToLocalUseCase(events)
                _uiState.update {
                    it.copy(
                        isInitialized = if (setInitialized) true else it.isInitialized,
                        isAuthorized = true,
                        isLoading = false,
                        events = events,
                        failureType = null
                    )
                }
                loadedRangeStart = rangeStart
                loadedRangeEnd = rangeEnd
                globalUiManager.emitToast(R.string.toast_holiday_failure)
            }
        }
    }

    /**
     * OnResume用: カレンダーAPI実行のみ
     * Dialog 表示なし。失敗時は failureType をセット、成功時は events/holidays を更新し failureType をクリア。
     *
     * トークン取得結果:
     * - NeedsConsent → 即座に再連携UI（連携解除と判明したため）
     * - TransientFailure → サイレント（一時的失敗の可能性。UI 維持）
     * - Success → API 実行。401 なら強制リフレッシュして1回リトライ
     */
    fun runCalendarApiOnly() {
        viewModelScope.launch {
            // 機内モード+WiFi未接続なら GMS 認証要求を待たず即ネットワークエラー判定
            if (networkAvailabilityChecker.isOfflineDueToAirplaneMode()) {
                applyRefreshableFailure(CalendarFailureType.API_NETWORK_ERROR, setInitialized = false)
                return@launch
            }

            val token = when (val r = googleAuthTokenProvider.requestAccessToken(appContext)) {
                is TokenResult.Success -> r.token
                TokenResult.NeedsConsent -> {
                    applyAuthFailure(setInitialized = false)
                    return@launch
                }
                TokenResult.TransientFailure -> {
                    return@launch
                }
            }

            val rangeStart = YearMonth.now().minusMonths(12)
            val rangeEnd = YearMonth.now().plusMonths(12)
            val req = buildCalendarReq(rangeStart, rangeEnd)

            _uiState.update { it.copy(isLoading = true) }

            val firstFetch = fetchEventsAndHolidays(req, rangeStart, rangeEnd, token)

            // 401 ならトークン強制リフレッシュして1回リトライ
            val (eventsResult, holidaysResult) = if (
                firstFetch.first is CalendarFetchResult.Failure
                && (firstFetch.first as CalendarFetchResult.Failure).type == CalendarFailureType.API_UNAUTHORIZED
            ) {
                when (val r = googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = true)) {
                    is TokenResult.Success -> {
                        fetchEventsAndHolidays(req, rangeStart, rangeEnd, r.token)
                    }
                    TokenResult.NeedsConsent -> {
                        applyAuthFailure(setInitialized = false)
                        return@launch
                    }
                    TokenResult.TransientFailure -> {
                        applyRefreshableFailure(CalendarFailureType.API_NETWORK_ERROR, setInitialized = false)
                        return@launch
                    }
                }
            } else {
                firstFetch
            }

            handleApiResult(
                eventsResult = eventsResult,
                holidaysResult = holidaysResult,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
                setInitialized = false
            )
        }
    }

    private fun buildCalendarReq(rangeStart: YearMonth, rangeEnd: YearMonth): CalendarReq {
        val timeMin = rangeStart.atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val timeMax = rangeEnd.atEndOfMonth().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return CalendarReq(timeMin = timeMin, timeMax = timeMax)
    }

    private suspend fun fetchEventsResult(req: CalendarReq, token: String): CalendarFetchResult {
        var result: CalendarFetchResult = CalendarFetchResult.Failure(CalendarFailureType.API_UNKNOWN)
        getCalendarUseCase(req, token).collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    val events = (resource.data?.items ?: emptyList())
                        .distinctBy { it.id }
                        .sortedBy { it.start }
                    result = CalendarFetchResult.Success(events)
                }
                is Resource.Error -> {
                    result = CalendarFetchResult.Failure(
                        CalendarFailureType.fromApiError(resource.cause)
                    )
                }
                is Resource.Loading -> {}
            }
        }
        return result
    }

    private suspend fun fetchHolidaysResult(
        rangeStart: YearMonth,
        rangeEnd: YearMonth,
        token: String
    ): List<HolidayEvent>? {
        return try {
            val startStr = "${rangeStart.year}-${rangeStart.monthValue.toString().padStart(2, '0')}"
            val endStr = "${rangeEnd.year}-${rangeEnd.monthValue.toString().padStart(2, '0')}"
            getHolidaysUseCase(startStr, endStr, token)
        } catch (e: Exception) {
            null
        }
    }

    private sealed class CalendarFetchResult {
        data class Success(val events: List<CalendarEvent>) : CalendarFetchResult()
        data class Failure(val type: CalendarFailureType) : CalendarFetchResult()
    }

    /**
     * 再認証成功後にAppScreenから呼ばれる。
     * カレンダーデータを ±12ヶ月分リロードする。
     */
    fun onReauthCompleted() {
        _uiState.update { it.copy(isAuthorized = true, failureType = null) }
        runCalendarApiOnly()
    }

    fun onMonthChange(yearMonth: YearMonth) {
        if (yearMonth == _uiState.value.currentYearMonth) return
        _uiState.update { it.copy(currentYearMonth = yearMonth) }
        checkAndPrefetch(yearMonth)
    }

    /**
     * プリフェッチ判定（Figma 133:2620 月移動時）
     * 進行方向の予定のストックが半年を切ったら、進行方向と同じ方向に12ヶ月分のカレンダーAPIを呼び出す。
     */
    private fun checkAndPrefetch(yearMonth: YearMonth) {
        // 未来方向ストック: 表示月から読み込み済み範囲の終端までの月数
        val futureStock = ChronoUnit.MONTHS.between(yearMonth, loadedRangeEnd)
        if (futureStock < 6 && !isPrefetchingFuture) {
            val newEnd = loadedRangeEnd.plusMonths(12)
            loadDateRange(loadedRangeEnd, newEnd, isFuture = true)
            prefetchHolidays(yearMonth)
        }

        // 過去方向ストック: 読み込み済み範囲の始端から表示月までの月数
        val pastStock = ChronoUnit.MONTHS.between(loadedRangeStart, yearMonth)
        if (pastStock < 6 && !isPrefetchingPast) {
            val newStart = loadedRangeStart.minusMonths(12)
            loadDateRange(newStart, loadedRangeStart, isFuture = false)
            prefetchHolidays(yearMonth)
        }
    }

    /**
     * 祝日プリフェッチ: 表示月を中心に前後12ヶ月を再取得し、既存データにマージ
     */
    private fun prefetchHolidays(yearMonth: YearMonth) {
        val token = googleAuthTokenProvider.getCachedToken() ?: return
        val newStart = yearMonth.minusMonths(12)
        val newEnd = yearMonth.plusMonths(12)

        // 既に範囲内なら不要
        if (newStart >= holidayLoadedRangeStart && newEnd <= holidayLoadedRangeEnd) return

        val startStr = "${newStart.year}-${newStart.monthValue.toString().padStart(2, '0')}"
        val endStr = "${newEnd.year}-${newEnd.monthValue.toString().padStart(2, '0')}"

        viewModelScope.launch {
            try {
                val holidays = getHolidaysUseCase(startStr, endStr, token)
                _uiState.update { state ->
                    val merged = (state.holidays + holidays)
                        .distinctBy { it.date }
                        .sortedBy { it.date }
                    state.copy(holidays = merged)
                }
                // 範囲を拡張
                if (newStart < holidayLoadedRangeStart) holidayLoadedRangeStart = newStart
                if (newEnd > holidayLoadedRangeEnd) holidayLoadedRangeEnd = newEnd
            } catch (e: Exception) {
                Log.e(TAG, "prefetchHolidays: ${e.message}", e)
            }
        }
    }

    /**
     * 指定した日付範囲のイベントを取得し、既存イベントにマージする（Figma 133:2620 月移動時）
     * トークン失効 → failureType=API_UNAUTHORIZED、その他失敗 → ErrorShortToast
     */
    private fun loadDateRange(rangeStart: YearMonth, rangeEnd: YearMonth, isFuture: Boolean) {
        if (isFuture) isPrefetchingFuture = true else isPrefetchingPast = true

        viewModelScope.launch {
            try {
                val token = when (val r = googleAuthTokenProvider.requestAccessToken(appContext)) {
                    is TokenResult.Success -> r.token
                    TokenResult.NeedsConsent -> {
                        applyAuthFailure(setInitialized = false)
                        return@launch
                    }
                    TokenResult.TransientFailure -> {
                        return@launch
                    }
                }

                val req = buildCalendarReq(rangeStart, rangeEnd)

                val firstResult = fetchEventsResult(req, token)

                // 401 ならトークン強制リフレッシュして1回リトライ
                val result = if (
                    firstResult is CalendarFetchResult.Failure
                    && firstResult.type == CalendarFailureType.API_UNAUTHORIZED
                ) {
                    when (val r = googleAuthTokenProvider.requestAccessToken(appContext, forceRefresh = true)) {
                        is TokenResult.Success -> {
                            fetchEventsResult(req, r.token)
                        }
                        TokenResult.NeedsConsent -> {
                            applyAuthFailure(setInitialized = false)
                            return@launch
                        }
                        TokenResult.TransientFailure -> {
                            applyRefreshableFailure(CalendarFailureType.API_NETWORK_ERROR, setInitialized = false)
                            return@launch
                        }
                    }
                } else {
                    firstResult
                }

                when (result) {
                    is CalendarFetchResult.Success -> {
                        _uiState.update { state ->
                            val merged = ((state.events ?: emptyList()) + result.events)
                                .distinctBy { it.id }
                                .sortedBy { it.start }
                            state.copy(events = merged)
                        }
                        if (isFuture) loadedRangeEnd = rangeEnd else loadedRangeStart = rangeStart
                        syncCalendarEventsToLocalUseCase(_uiState.value.events ?: emptyList())
                    }
                    is CalendarFetchResult.Failure -> {
                        if (result.type == CalendarFailureType.API_UNAUTHORIZED) {
                            // リトライ後も 401 = 連携解除ではない特殊状態 → リフレッシュボタン表示
                            applyRefreshableFailure(CalendarFailureType.API_UNKNOWN, setInitialized = false)
                        } else {
                            emitCalendarFailureToast(result.type)
                        }
                    }
                }
            } finally {
                if (isFuture) isPrefetchingFuture = false else isPrefetchingPast = false
            }
        }
    }

    /** 月セルタップで日付詳細オーバーレイを開く */
    fun onOpenDayOverlay(date: LocalDate) {
        _uiState.update { it.copy(overlayDate = date) }
    }

    /** システム戻るボタンで日付詳細オーバーレイを閉じる */
    fun onCloseDayOverlay() {
        _uiState.update { it.copy(overlayDate = null) }
    }

    /** オーバーレイ内で左右スワイプにより表示日が変わった時に呼ぶ */
    fun onOverlayDateChanged(date: LocalDate) {
        _uiState.update { it.copy(overlayDate = date) }
    }

    /**
     * カレンダーUIを初期状態にリセット（ナビゲーションバーで他画面へ遷移した時に呼ばれる）。
     * - 表示月/選択日を「現在月/今日」に戻す
     * - 日付詳細オーバーレイを閉じる
     * - resetUiSignal を emit して CalendarScreen 側の Pager を初期ページへスナップさせる
     */
    fun resetCalendarUi() {
        val now = YearMonth.now()
        _uiState.update {
            it.copy(
                currentYearMonth = now,
                overlayDate = null
            )
        }
        _resetUiSignal.tryEmit(Unit)
    }

    /**
     * Google連携解除時の状態リセット。
     * in-memory の events / holidays / isAuthorized / failureType をクリアし、
     * 再ログインまでホーム画面の「直近の予定」が前アカウントの内容を表示しないようにする。
     */
    fun onLogout() {
        _uiState.update {
            it.copy(
                isAuthorized = false,
                events = null,
                holidays = emptyList(),
                failureType = null,
                overlayDate = null
            )
        }
        loadedRangeStart = YearMonth.now().minusMonths(12)
        loadedRangeEnd = YearMonth.now().plusMonths(12)
        holidayLoadedRangeStart = YearMonth.now().minusMonths(12)
        holidayLoadedRangeEnd = YearMonth.now().plusMonths(12)
    }

    fun onDeleteEvent(eventId: String) {
        viewModelScope.launch {
            val token = googleAuthTokenProvider.ensureAccessToken(appContext) ?: run {
                handleAuthFailure()
                return@launch
            }
            val target = _uiState.value.events?.firstOrNull { it.id == eventId }
            deleteCalendarEventUseCase(token, eventId)
                .onSuccess {
                    val affected = target?.start?.let(::parseEventDate)?.let { setOf(it) } ?: emptySet()
                    if (affected.isNotEmpty()) reloadEventsForDates(affected, token)
                }
                .onFailure { e -> handleApiError(e, CrudOperation.DELETE) }
        }
    }

    fun onEditEvent(eventId: String, body: CreateEventReq) {
        viewModelScope.launch {
            val token = googleAuthTokenProvider.ensureAccessToken(appContext) ?: run {
                handleAuthFailure()
                return@launch
            }
            val oldEvent = _uiState.value.events?.firstOrNull { it.id == eventId }
            updateCalendarEventUseCase(token, eventId, body)
                .onSuccess { updated ->
                    val affected = buildSet {
                        oldEvent?.start?.let(::parseEventDate)?.let { add(it) }
                        parseEventDate(updated.start)?.let { add(it) }
                    }
                    if (affected.isNotEmpty()) reloadEventsForDates(affected, token)
                }
                .onFailure { e -> handleApiError(e, CrudOperation.EDIT) }
        }
    }

    fun onCreateEvent(body: CreateEventReq) {
        viewModelScope.launch {
            val token = googleAuthTokenProvider.ensureAccessToken(appContext) ?: run {
                handleAuthFailure()
                return@launch
            }
            createCalendarEventUseCase(token, body)
                .onSuccess { created ->
                    val affected = parseEventDate(created.start)?.let { setOf(it) } ?: emptySet()
                    if (affected.isNotEmpty()) reloadEventsForDates(affected, token)
                }
                .onFailure { e -> handleApiError(e, CrudOperation.CREATE) }
        }
    }

    /**
     * 影響を受けた日付の予定だけをカレンダーAPIから再取得し、UI events をマージ更新する。
     * UI 反映後、events 全体を Local に保存する（Widget 更新も Repository 経由で連動）。
     */
    private suspend fun reloadEventsForDates(dates: Set<LocalDate>, token: String) {
        val results = coroutineScope {
            dates.map { date ->
                async {
                    val timeMin = date.atStartOfDay(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    val timeMax = date.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    fetchEventsResult(CalendarReq(timeMin = timeMin, timeMax = timeMax), token)
                }
            }.awaitAll()
        }

        val unauthorized = results.any {
            it is CalendarFetchResult.Failure && it.type == CalendarFailureType.API_UNAUTHORIZED
        }
        if (unauthorized) {
            googleAuthTokenProvider.clearCachedToken()
            _uiState.update {
                it.copy(
                    isAuthorized = false,
                    failureType = CalendarFailureType.API_UNAUTHORIZED,
                    overlayDate = null
                )
            }
            return
        }
        val failureTypes = results.filterIsInstance<CalendarFetchResult.Failure>().map { it.type }
        if (failureTypes.isNotEmpty()) {
            val type = if (CalendarFailureType.API_QUOTA_EXCEEDED in failureTypes) {
                CalendarFailureType.API_QUOTA_EXCEEDED
            } else {
                failureTypes.first()
            }
            emitCalendarFailureToast(type)
            return
        }

        val newEvents = results
            .filterIsInstance<CalendarFetchResult.Success>()
            .flatMap { it.events }

        _uiState.update { state ->
            val filtered = (state.events ?: emptyList()).filter { event ->
                val date = parseEventDate(event.start) ?: return@filter true
                date !in dates
            }
            val merged = (filtered + newEvents)
                .distinctBy { it.id }
                .sortedBy { it.start }
            state.copy(events = merged)
        }

        syncCalendarEventsToLocalUseCase(_uiState.value.events ?: emptyList())
    }

    private fun handleAuthFailure() {
        googleAuthTokenProvider.clearCachedToken()
        _uiState.update {
            it.copy(
                isAuthorized = false,
                failureType = CalendarFailureType.API_UNAUTHORIZED,
                overlayDate = null
            )
        }
    }

    private enum class CrudOperation { CREATE, EDIT, DELETE }

    private fun handleApiError(e: Throwable, operation: CrudOperation) {
        when (CalendarFailureType.fromApiError(e)) {
            CalendarFailureType.API_UNAUTHORIZED -> handleAuthFailure()
            CalendarFailureType.API_QUOTA_EXCEEDED ->
                globalUiManager.emitToast(R.string.toast_calendar_quota_exceeded)
            else -> {
                val resId = when (operation) {
                    CrudOperation.CREATE -> R.string.toast_event_create_failure
                    CrudOperation.EDIT -> R.string.toast_event_edit_failure
                    CrudOperation.DELETE -> R.string.toast_event_delete_failure
                }
                globalUiManager.emitToast(resId)
            }
        }
    }

    private fun emitCalendarFailureToast(type: CalendarFailureType) {
        val resId = if (type == CalendarFailureType.API_QUOTA_EXCEEDED) {
            R.string.toast_calendar_quota_exceeded
        } else {
            R.string.toast_calendar_failure
        }
        globalUiManager.emitToast(resId)
    }

}
