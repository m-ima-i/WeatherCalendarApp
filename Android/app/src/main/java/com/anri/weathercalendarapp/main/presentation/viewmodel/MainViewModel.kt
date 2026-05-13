package com.anri.weathercalendarapp.main.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.common.auth.GoogleAuthTokenProvider
import com.anri.weathercalendarapp.main.presentation.GlobalUiManager
import com.anri.weathercalendarapp.main.presentation.event.AppScreenUiEvent
import com.anri.weathercalendarapp.main.presentation.event.OnResumeEvent
import com.anri.weathercalendarapp.main.presentation.event.StartupEvent
import com.anri.weathercalendarapp.main.presentation.state.MainUiState
import com.anri.weathercalendarapp.main.presentation.state.StartupStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val googleAuthTokenProvider: GoogleAuthTokenProvider,
    private val globalUiManager: GlobalUiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /** 起動プロセスの各フェーズ Event */
    private val _startupEvent = MutableSharedFlow<StartupEvent>(replay = 1)
    val startupEvent: SharedFlow<StartupEvent> = _startupEvent.asSharedFlow()

    /** OnResume プロセスの Event */
    private val _onResumeEvent = MutableSharedFlow<OnResumeEvent>(replay = 1)
    val onResumeEvent: SharedFlow<OnResumeEvent> = _onResumeEvent.asSharedFlow()

    /** 認証関連の UI Event（Activity 側で処理が必要なもの） */
    private val _uiEvent = MutableSharedFlow<AppScreenUiEvent>()
    val uiEvent: SharedFlow<AppScreenUiEvent> = _uiEvent.asSharedFlow()

    /** GPS Resolution リクエスト要求（MainActivity が IntentSender 経由で起動） */
    private val _requestGpsResolution = MutableSharedFlow<android.content.IntentSender>()
    val requestGpsResolution: SharedFlow<android.content.IntentSender> = _requestGpsResolution.asSharedFlow()

    /** onCreate 直後の onResume での二重 emit 防止フラグ */
    private var isFirstResume = true

    private var pendingAuthSuccessCallback: (() -> Unit)? = null

    // ── 起動プロセス制御 ──────────────────────────────

    /** App起動時に呼ぶ（onCreate） */
    fun startApp() {
        if (_uiState.value.startupStep == StartupStep.COMPLETED) return
        viewModelScope.launch {
            _startupEvent.emit(StartupEvent.LoadLocalWeather)
        }
    }

    /** Local天気取得完了 → 天気APIプロセスとカレンダーAPIプロセスを並列で開始 */
    fun onLocalWeatherLoaded() {
        _uiState.update { it.copy(startupStep = StartupStep.API_PROCESSES) }
        viewModelScope.launch {
            _startupEvent.emit(StartupEvent.RunWeatherProcess)
            _startupEvent.emit(StartupEvent.RunCalendarProcess)
        }
    }

    /** 天気APIプロセス完了 → 両方完了していれば起動完了へ */
    fun onWeatherProcessCompleted() {
        _uiState.update { it.copy(weatherProcessCompleted = true) }
        completeIfAllApiProcessesDone()
    }

    /** カレンダーAPIプロセス完了 → 両方完了していれば起動完了へ */
    fun onCalendarProcessCompleted() {
        _uiState.update { it.copy(calendarProcessCompleted = true) }
        completeIfAllApiProcessesDone()
    }

    private fun completeIfAllApiProcessesDone() {
        val s = _uiState.value
        if (s.weatherProcessCompleted && s.calendarProcessCompleted) {
            _uiState.update { it.copy(startupStep = StartupStep.COMPLETED) }
        }
    }

    /**
     * OnResume時に呼ぶ。
     * - 初回（onCreate 直後）はスキップ（startApp との二重 emit 回避）
     * - COMPLETED → OnResumeEvent.RunApis
     * - API_PROCESSES → 未完了のプロセスのみ再 emit（並列実行を継続）
     * - LOAD_LOCAL_WEATHER → 何もしない（進行中の loadLocalWeather coroutine の完了で次ステップへ自動遷移する）
     */
    fun onAppResumed() {
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        when (_uiState.value.startupStep) {
            StartupStep.COMPLETED -> viewModelScope.launch {
                _onResumeEvent.emit(OnResumeEvent.RunApis)
            }
            StartupStep.API_PROCESSES -> {
                val s = _uiState.value
                viewModelScope.launch {
                    if (!s.weatherProcessCompleted) {
                        _startupEvent.emit(StartupEvent.RunWeatherProcess)
                    }
                    if (!s.calendarProcessCompleted) {
                        _startupEvent.emit(StartupEvent.RunCalendarProcess)
                    }
                }
            }
            StartupStep.LOAD_LOCAL_WEATHER -> Unit
        }
    }

    /** GPS Resolution Dialog を発火（SettingsClient の Resolvable Exception 経由） */
    fun requestGpsResolution(intentSender: android.content.IntentSender) {
        viewModelScope.launch {
            _requestGpsResolution.emit(intentSender)
        }
    }

    /**
     * GPS Resolution の結果（MainActivity の gpsResolutionLauncher から）。
     * 許可・拒否いずれの場合も天気APIプロセスを再実行する。
     * 2回目以降は gpsDialogShown=true により Dialog 経路をスキップして処理終了へ進む。
     */
    fun onGpsResolutionResult(@Suppress("UNUSED_PARAMETER") granted: Boolean) {
        viewModelScope.launch {
            _startupEvent.emit(StartupEvent.RunWeatherProcess)
        }
    }

    // ── 認証関連 ──────────────────────────────

    fun onRequestLogin(
        context: Context,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            pendingAuthSuccessCallback = onSuccess
            try {
                val result = googleAuthTokenProvider.authorize(context)
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    if (pendingIntent != null) {
                        _uiEvent.emit(AppScreenUiEvent.LaunchConsent(pendingIntent))
                    }
                } else {
                    val token = result.accessToken
                    if (token != null) {
                        onAuthSuccess(token)
                    }
                }
            } catch (e: Exception) {
                onAuthFailed()
            }
        }
    }

    fun onAuthSuccess(token: String, email: String? = null) {
        viewModelScope.launch {
            // consent flow で取得したトークンを即座にキャッシュ。
            // これがないと後続の ensureAccessToken が authorize() を再呼び出しする際に
            // hasResolution=true で null が返り、API呼び出しがスキップされる事象を防ぐ。
            googleAuthTokenProvider.setCachedToken(token)

            val resolvedEmail = email
                ?: googleAuthTokenProvider.fetchAccountEmail(token)

            if (resolvedEmail == null) {
                onAuthFailed()
                return@launch
            }

            authPreferences.setAccountEmail(resolvedEmail)

            pendingAuthSuccessCallback?.invoke()
            pendingAuthSuccessCallback = null

            _uiEvent.emit(AppScreenUiEvent.AuthSucceeded)
        }
    }

    fun onAuthFailed() {
        pendingAuthSuccessCallback = null
        globalUiManager.emitToast(R.string.toast_google_auth_failure)
    }

}
