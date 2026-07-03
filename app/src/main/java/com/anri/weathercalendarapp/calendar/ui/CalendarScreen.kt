package com.anri.weathercalendarapp.calendar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.presentation.state.CalendarUiState
import com.anri.weathercalendarapp.calendar.presentation.type.CalendarFailureType
import com.anri.weathercalendarapp.calendar.presentation.viewmodel.CalendarViewModel
import com.anri.weathercalendarapp.calendar.ui.common.CalendarFailureContent
import com.anri.weathercalendarapp.calendar.ui.components.DayOverlayContent
import com.anri.weathercalendarapp.calendar.ui.components.MonthCalendarGrid
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onRequestLogin: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    val failureType = uiState.failureType
    val hasEvents = uiState.events != null
    // 全画面失敗UIに遷移する条件:
    //  - 連携解除(API_UNAUTHORIZED): events 有無関係なく必ず全画面（再連携誘導）
    //  - その他 failure: events 未取得時のみ全画面（取得済みなら HOME のバナーで通知し、当画面は月画面を維持）
    val showFullScreenFailure = failureType == CalendarFailureType.API_UNAUTHORIZED ||
            (failureType != null && !hasEvents)

    when {
        showFullScreenFailure -> {
            CalendarFailureContent(
                failureType = failureType,
                isLoading = uiState.isLoading,
                onRequestReauth = onRequestLogin,
                onRefresh = viewModel::runCalendarApiOnly
            )
        }
        !uiState.isAuthorized -> {
            UnauthorizedCalendarContent(
                onRequestAuth = onRequestLogin
            )
        }
        else -> {
            CalendarContent(
                uiState = uiState,
                resetUiSignal = viewModel.resetUiSignal,
                onMonthChange = viewModel::onMonthChange,
                onOpenDayOverlay = viewModel::onOpenDayOverlay,
                onCloseDayOverlay = viewModel::onCloseDayOverlay,
                onOverlayDateChanged = viewModel::onOverlayDateChanged,
                onCreateEvent = viewModel::onCreateEvent,
                onEditEvent = viewModel::onEditEvent,
                onDeleteEvent = viewModel::onDeleteEvent
            )
        }
    }
}

/** 未認証時のカレンダー画面: 中央に連携誘導テキストとボタンを表示 */
@Composable
private fun UnauthorizedCalendarContent(
    onRequestAuth: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.calendar_login_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRequestAuth) {
                Text(text = stringResource(R.string.calendar_login_button))
            }
        }
    }
}

@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    resetUiSignal: SharedFlow<Unit>,
    onMonthChange: (YearMonth) -> Unit,
    onOpenDayOverlay: (LocalDate) -> Unit,
    onCloseDayOverlay: () -> Unit,
    onOverlayDateChanged: (LocalDate) -> Unit,
    onCreateEvent: (CreateEventReq) -> Unit,
    onEditEvent: (String, CreateEventReq) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    val initialPage = 1200
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2400 })
    val coroutineScope = rememberCoroutineScope()

    val baseYearMonth = remember { YearMonth.now() }

    val displayedYearMonth = remember(pagerState.currentPage) {
        baseYearMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val offset = page - initialPage
            val newYearMonth = baseYearMonth.plusMonths(offset.toLong())
            onMonthChange(newYearMonth)
        }
    }

    // ナビゲーションバー遷移時に Pager を初期月（=今日の月）へスナップ
    LaunchedEffect(resetUiSignal) {
        resetUiSignal.collect {
            pagerState.scrollToPage(initialPage)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 3.dp, end = 3.dp, top = 12.dp, bottom = 16.dp)
            ) {
                MonthNavigationHeader(
                    yearMonth = displayedYearMonth,
                    onPrevMonth = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    onNextMonth = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val offset = page - initialPage
                    val pageYearMonth = baseYearMonth.plusMonths(offset.toLong())

                    val pageEvents = remember(pageYearMonth, uiState.events) {
                        val monthStart = pageYearMonth.atDay(1)
                        val gridStart = monthStart.minusDays(
                            (monthStart.dayOfWeek.value % 7).toLong()
                        )
                        val gridEnd = gridStart.plusDays(41)
                        (uiState.events ?: emptyList()).filter { item ->
                            val startDate = parseDate(item.start) ?: return@filter false
                            val endDate = item.end?.let { parseDate(it) }
                            if (endDate == null) {
                                startDate in gridStart..gridEnd
                            } else if (item.isAllDayEvent) {
                                startDate < gridEnd.plusDays(1) && endDate > gridStart
                            } else {
                                startDate <= gridEnd && endDate >= gridStart
                            }
                        }
                    }
                    val pageHolidays = remember(pageYearMonth, uiState.holidays, uiState.showHolidays) {
                        if (!uiState.showHolidays) {
                            emptyList()
                        } else {
                            val firstDay = pageYearMonth.atDay(1)
                            val gStart = firstDay.minusDays((firstDay.dayOfWeek.value % 7).toLong())
                            val gEnd = gStart.plusDays(41)
                            uiState.holidays.filter { it.date in gStart..gEnd }
                        }
                    }

                    MonthCalendarGrid(
                        yearMonth = pageYearMonth,
                        events = pageEvents,
                        holidays = pageHolidays,
                        onDateSelected = onOpenDayOverlay,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 日付詳細オーバーレイ（uiState.overlayDate != null の時に表示）
        uiState.overlayDate?.let { overlayDate ->
            DayOverlayContent(
                date = overlayDate,
                events = uiState.events ?: emptyList(),
                holidays = if (uiState.showHolidays) uiState.holidays else emptyList(),
                eventColors = uiState.eventColors,
                onClose = onCloseDayOverlay,
                onDateChange = onOverlayDateChanged,
                onCreateEvent = onCreateEvent,
                onEditEvent = onEditEvent,
                onDeleteEvent = onDeleteEvent
            )
        }
    }
}

@Composable
private fun MonthNavigationHeader(
    yearMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy年 M月", Locale.JAPANESE) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevMonth,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = yearMonth.format(formatter),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

private fun parseDate(dateTimeStr: String?): LocalDate? {
    if (dateTimeStr == null) return null
    return try {
        LocalDate.parse(dateTimeStr.substringBefore("T"))
    } catch (_: Exception) {
        null
    }
}
