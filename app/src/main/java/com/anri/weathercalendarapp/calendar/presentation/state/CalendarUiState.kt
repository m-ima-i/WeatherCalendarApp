package com.anri.weathercalendarapp.calendar.presentation.state

import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.presentation.type.CalendarFailureType
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    // 初回認証チェック完了フラグ（完了するまでUI表示を抑制）
    val isInitialized: Boolean = false,
    // 認証
    val isAuthorized: Boolean = false,
    // カレンダーデータ
    val currentYearMonth: YearMonth = YearMonth.now(),
    // null = 未取得（ローダー表示）, emptyList = 取得完了で予定なし
    val events: List<CalendarEvent>? = null,
    val holidays: List<HolidayEvent> = emptyList(),
    val isLoading: Boolean = false,
    // API 失敗種別。null = 失敗なし
    val failureType: CalendarFailureType? = null,
    // イベントカラーパレット（colorId → backgroundColor）
    val eventColors: Map<String, String> = emptyMap(),
    // 日付詳細オーバーレイの表示日付。null = 月画面表示中
    val overlayDate: LocalDate? = null,
    // 祝日表示フラグ（false の時はカレンダー画面で祝日を非表示にする）
    val showHolidays: Boolean = true
)
