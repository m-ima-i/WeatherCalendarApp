package com.anri.weathercalendarapp.calendar.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.displayTitle
import com.anri.weathercalendarapp.common.view.dialog.DeleteEventConfirmDialog
import java.time.LocalDate
import java.time.LocalTime

private const val INITIAL_PAGE = 1200
private const val PAGE_COUNT = 2400

/**
 * 月画面コンテンツ部分を覆う日付詳細オーバーレイ。
 * - 背景は不透明（surfaceContainerHigh）
 * - システム戻るボタンで閉じる（背景タップでは閉じない）
 * - 左右スワイプ（HorizontalPager）で前後の日付に移動
 * - 各ページに AllDayHeader + TimelineView + 右下 FAB
 * - failureType がセットされた場合は親側で overlayDate=null になり当 Composable 自体が消える
 */
@Composable
fun DayOverlayContent(
    date: LocalDate,
    events: List<CalendarEvent>,
    holidays: List<HolidayEvent>,
    eventColors: Map<String, String>,
    onClose: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onCreateEvent: (CreateEventReq) -> Unit,
    onEditEvent: (String, CreateEventReq) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    BackHandler(onBack = onClose)

    var addEventTrigger by remember { mutableStateOf<AddEventTrigger?>(null) }
    var editTarget by remember { mutableStateOf<CalendarEvent?>(null) }
    var deleteTarget by remember { mutableStateOf<Pair<String, String>?>(null) }

    // 起動時の date を中心にページング
    val baseDate = remember { date }
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE, pageCount = { PAGE_COUNT })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val newDate = baseDate.plusDays((page - INITIAL_PAGE).toLong())
            onDateChange(newDate)
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageDate = baseDate.plusDays((page - INITIAL_PAGE).toLong())
                val pageEvents = remember(events, pageDate) {
                    events.filter { isEventOnDate(it, pageDate) }
                }
                val pageAllDay = pageEvents.filter { it.isAllDayEvent }
                val pageTimed = pageEvents.filterNot { it.isAllDayEvent }
                val pageHolidays = remember(holidays, pageDate) {
                    holidays.filter { it.date == pageDate }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 5.dp)
                ) {
                    AllDayHeader(
                        date = pageDate,
                        allDayEvents = pageAllDay,
                        holidays = pageHolidays,
                        onEventClick = { editTarget = it },
                        onAddAllDay = {
                            addEventTrigger = AddEventTrigger(date = pageDate)
                        }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    TimelineView(
                        date = pageDate,
                        events = pageTimed,
                        onAddEventAt = { time ->
                            addEventTrigger = AddEventTrigger(
                                date = pageDate,
                                startTime = time,
                                endTime = time.plusHours(1)
                            )
                        },
                        onEventClick = { editTarget = it }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                addEventTrigger = AddEventTrigger(date = date)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }
    }

    addEventTrigger?.let { trigger ->
        AddEventDialog(
            date = trigger.date,
            eventColors = eventColors,
            onDismiss = { addEventTrigger = null },
            onConfirm = { req ->
                onCreateEvent(req)
                addEventTrigger = null
            },
            initialStartTime = trigger.startTime,
            initialEndTime = trigger.endTime
        )
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

private data class AddEventTrigger(
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null
)

/**
 * イベントが指定日に該当するか判定（CalendarScreen の isEventOnDate と同じロジック）
 */
private fun isEventOnDate(event: CalendarEvent, date: LocalDate): Boolean {
    val startDate = parseEventLocalDate(event.start) ?: return false
    val endDate = parseEventLocalDate(event.end) ?: return startDate == date

    return if (event.isAllDayEvent) {
        date >= startDate && date < endDate
    } else {
        date in startDate..endDate
    }
}
