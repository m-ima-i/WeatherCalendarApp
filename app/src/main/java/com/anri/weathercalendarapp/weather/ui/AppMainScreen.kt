package com.anri.weathercalendarapp.weather.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.displayTitle
import com.anri.weathercalendarapp.calendar.presentation.type.CalendarFailureType
import com.anri.weathercalendarapp.calendar.ui.common.CalendarFailureBanner
import com.anri.weathercalendarapp.calendar.ui.components.EditEventDialog
import com.anri.weathercalendarapp.common.view.CustomElevateCared
import com.anri.weathercalendarapp.common.view.dialog.DeleteEventConfirmDialog
import com.anri.weathercalendarapp.weather.domain.model.response.UpcomingEventWithWeather
import com.anri.weathercalendarapp.weather.presentation.state.WeatherUiState
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.weather.ui.common.CurrentWeatherCard
import com.anri.weathercalendarapp.weather.ui.common.HourlyWeatherRow
import com.anri.weathercalendarapp.weather.ui.common.WeatherFailureBanner
import com.anri.weathercalendarapp.weather.ui.common.WeatherFailureContent
import com.anri.weathercalendarapp.weather.ui.mainscreen.CalendarColumn

@Composable
fun AppMainScreen(
    weatherUiState: WeatherUiState,
    upcomingEvents: List<UpcomingEventWithWeather>? = null,
    calendarEvents: List<CalendarEvent>? = null,
    eventColors: Map<String, String> = emptyMap(),
    isCalendarInitialized: Boolean = false,
    isCalendarAuthorized: Boolean = false,
    calendarFailureType: CalendarFailureType? = null,
    isCalendarLoading: Boolean = false,
    onRequestCalendarLogin: () -> Unit = {},
    onRefreshCalendar: () -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    onOpenLocationSettings: () -> Unit = {},
    onOpenGpsSettings: () -> Unit = {},
    onEditEvent: (String, CreateEventReq) -> Unit = { _, _ -> },
    onDeleteEvent: (String) -> Unit = {},
) {
    var editTarget by remember { mutableStateOf<CalendarEvent?>(null) }
    var deleteTarget by remember { mutableStateOf<Pair<String, String>?>(null) }

    val current = weatherUiState.weather?.current
    val hourly = weatherUiState.weather?.hourly
    val daily = weatherUiState.weather?.daily

    val currentWeatherType = WeatherType.fromId(current?.weather?.firstOrNull()?.icon)
    val isSuccess = weatherUiState.weather != null
    val showWeatherBanner = isSuccess && weatherUiState.failureType != null

    // Calendar バナー表示条件:
    //  - failureType は API_UNAUTHORIZED 以外（再連携必要時は CalendarColumn 内に全画面失敗UIを出す）
    //  - upcomingEvents が取得済み（キャッシュあり）→ バナー + キャッシュ表示
    //  - キャッシュなしの時は CalendarColumn 内の従来UIに任せる
    val isRefreshableCalendarFailure = calendarFailureType != null &&
            calendarFailureType != CalendarFailureType.API_UNAUTHORIZED
    val hasUpcomingEvents = !upcomingEvents.isNullOrEmpty()
    val showCalendarBanner = isRefreshableCalendarFailure && hasUpcomingEvents

    val anyBanner = showWeatherBanner || showCalendarBanner
    // バナー表示時のみ CalendarColumn 内の failureType を抑制（events を表示し続ける）
    val calendarFailureForColumn = if (showCalendarBanner) null else calendarFailureType

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val totalHeightPx = constraints.maxHeight
        val verticalPaddingPx = with(density) { 20.dp.roundToPx() } // outer Column の padding 10dp × 2
        val bottomSpacerPx = with(density) { 10.dp.roundToPx() } // 天気カード群と CalendarColumn の間
        // CalendarColumn の高さは「天気カード群の高さ」のみを基準に算出する。
        // バナー分は加算せず、バナー表示時はオーバーフローして verticalScroll で見せる。
        var weatherSectionHeightPx by remember { mutableIntStateOf(0) }

        val calendarHeightDp = with(density) {
            val remaining = totalHeightPx - verticalPaddingPx - bottomSpacerPx - weatherSectionHeightPx
            if (remaining > 0) remaining.toDp() else 0.dp
        }

        val scrollableModifier = if (anyBanner) {
            Modifier.verticalScroll(rememberScrollState())
        } else {
            Modifier
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(scrollableModifier)
                .padding(10.dp)
        ) {
            // バナー群（高さ計測対象外。表示時は画面外にオーバーフローし scroll で見せる）
            if (showWeatherBanner) {
                WeatherFailureBanner(
                    failureType = weatherUiState.failureType!!,
                    onRefresh = onRefreshWeather,
                    onOpenLocationSettings = onOpenLocationSettings,
                    onOpenGpsSettings = onOpenGpsSettings,
                    isLoading = weatherUiState.isLoading,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            if (showCalendarBanner) {
                CalendarFailureBanner(
                    onRefresh = onRefreshCalendar,
                    isLoading = isCalendarLoading,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 天気カード群（高さ計測対象 = calendarHeightDp の基準）
            Column(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    weatherSectionHeightPx = coordinates.size.height
                }
            ) {
                if (isSuccess) {
                    CurrentWeatherCard(
                        currentTemp = current?.temp ?: 0.0,
                        maxTemp = daily?.firstOrNull()?.temp?.max ?: 0.0,
                        minTemp = daily?.firstOrNull()?.temp?.min ?: 0.0,
                        feelsLike = current?.feelsLike ?: 0.0,
                        pop = hourly?.firstOrNull()?.pop ?: 0.0,
                        weatherType = currentWeatherType,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HourlyWeatherRow(
                        hourlyWeather = hourly,
                    )
                } else {
                    CustomElevateCared(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                    ) {
                        WeatherFailureContent(
                            failureType = weatherUiState.failureType,
                            isLoading = weatherUiState.isLoading
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // 直近の予定リスト（バナー有無に関わらず固定の高さを取る）
            if (calendarHeightDp > 0.dp) {
                CalendarColumn(
                    modifier = Modifier.height(calendarHeightDp),
                    upcomingEvents = upcomingEvents,
                    isCalendarInitialized = isCalendarInitialized,
                    isCalendarAuthorized = isCalendarAuthorized,
                    failureType = calendarFailureForColumn,
                    isLoading = isCalendarLoading,
                    onRequestLogin = onRequestCalendarLogin,
                    onRefresh = onRefreshCalendar,
                    onEventClick = { eventId ->
                        editTarget = calendarEvents?.firstOrNull { it.id == eventId }
                    }
                )
            }
        }
    }

    editTarget?.let { event ->
        EditEventDialog(
            event = event,
            eventColors = eventColors,
            onDismiss = { editTarget = null },
            onConfirm = { eventId, req ->
                onEditEvent(eventId, req)
                editTarget = null
            },
            onDelete = {
                deleteTarget = event.id to event.displayTitle()
                editTarget = null
            }
        )
    }

    deleteTarget?.let { (eventId, eventTitle) ->
        DeleteEventConfirmDialog(
            eventTitle = eventTitle,
            onConfirm = {
                onDeleteEvent(eventId)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}
