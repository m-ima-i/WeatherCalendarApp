package com.anri.weathercalendarapp.calendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.displayTitle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private const val MAX_BARS_PER_CELL = 2

private fun Modifier.cellBorder(
    color: Color,
    showBottom: Boolean,
    showEnd: Boolean,
    thickness: Dp = 1.dp
): Modifier = drawBehind {
    val px = thickness.toPx()
    val w = size.width
    val h = size.height
    if (showEnd) {
        drawLine(
            color = color,
            start = Offset(w - px / 2, 0f),
            end = Offset(w - px / 2, h),
            strokeWidth = px
        )
    }
    if (showBottom) {
        drawLine(
            color = color,
            start = Offset(0f, h - px / 2),
            end = Offset(w, h - px / 2),
            strokeWidth = px
        )
    }
}

@Composable
fun MonthCalendarGrid(
    yearMonth: YearMonth,
    events: List<CalendarEvent>,
    holidays: List<HolidayEvent>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    // 日曜始まり: SUNDAY=7 → offset 0, MONDAY=1 → offset 1, ...
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7)

    val prevYearMonth = yearMonth.minusMonths(1)
    val prevMonthDays = prevYearMonth.lengthOfMonth()
    val nextYearMonth = yearMonth.plusMonths(1)

    // 日付をまたぐイベントも各日に表示されるようにマッピング
    val eventsByDate = remember(events, yearMonth) {
        val map = mutableMapOf<LocalDate, MutableList<CalendarEvent>>()
        events.forEach { event ->
            val startDate = event.start?.let { parseDate(it) } ?: return@forEach
            val endDate = event.end?.let { parseDate(it) }

            if (endDate == null || startDate == endDate ||
                (!event.isAllDayEvent && startDate == endDate)) {
                map.getOrPut(startDate) { mutableListOf() }.add(event)
            } else if (event.isAllDayEvent) {
                var d = startDate
                while (d < endDate) {
                    map.getOrPut(d) { mutableListOf() }.add(event)
                    d = d.plusDays(1)
                }
            } else {
                var d = startDate
                while (d <= endDate) {
                    map.getOrPut(d) { mutableListOf() }.add(event)
                    d = d.plusDays(1)
                }
            }
        }
        map
    }

    val holidaysByDate = remember(holidays) {
        holidays.groupBy { it.date }
    }

    data class CellData(
        val date: LocalDate,
        val day: Int,
        val isCurrentMonth: Boolean
    )

    val cells = remember(yearMonth) {
        buildList {
            for (i in startDayOfWeek - 1 downTo 0) {
                val d = prevMonthDays - i
                add(CellData(prevYearMonth.atDay(d), d, false))
            }
            for (d in 1..daysInMonth) {
                add(CellData(yearMonth.atDay(d), d, true))
            }
            val remaining = 42 - size
            for (d in 1..remaining) {
                add(CellData(nextYearMonth.atDay(d), d, false))
            }
        }
    }

    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        WeekDayHeader(dividerColor = dividerColor)

        for (row in 0 until 6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val cell = cells[cellIndex]
                    val cellEvents = eventsByDate[cell.date] ?: emptyList()
                    val cellHolidays = holidaysByDate[cell.date] ?: emptyList()

                    DayCell(
                        day = cell.day,
                        isToday = cell.date == today,
                        isCurrentMonth = cell.isCurrentMonth,
                        isSunday = col == 0,
                        isSaturday = col == 6,
                        events = cellEvents,
                        holidays = cellHolidays,
                        onClick = { onDateSelected(cell.date) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .cellBorder(
                                color = dividerColor,
                                showBottom = row < 5,
                                showEnd = col < 6
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekDayHeader(dividerColor: Color) {
    val daysOfWeek = listOf(
        DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            val color = when (index) {
                0 -> MaterialTheme.colorScheme.error
                6 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.JAPANESE),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .cellBorder(
                        color = dividerColor,
                        showBottom = true,
                        showEnd = index < 6
                    )
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    isSunday: Boolean,
    isSaturday: Boolean,
    events: List<CalendarEvent>,
    holidays: List<HolidayEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHoliday = holidays.isNotEmpty()
    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        !isCurrentMonth -> outlineColor
        isHoliday || isSunday -> MaterialTheme.colorScheme.error
        isSaturday -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 30.dp, height = 20.dp)
                .then(
                    if (isToday) {
                        Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier
                    }
                )
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (holidays.isNotEmpty() || events.isNotEmpty()) {
            val bars = buildList {
                holidays.forEach { holiday ->
                    add(EventBarData(holiday.name, errorColor))
                }
                events.forEach { event ->
                    val color = parseEventColor(event.backgroundColor) ?: primaryColor
                    add(EventBarData(event.displayTitle(), color))
                }
            }.take(MAX_BARS_PER_CELL)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                bars.forEach { bar ->
                    EventBar(
                        title = bar.title,
                        color = bar.color
                    )
                }
            }
        }
    }
}

private data class EventBarData(val title: String, val color: Color)

@Composable
private fun EventBar(
    title: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(color)
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = readableTextColorOn(color),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start
        )
    }
}

/**
 * ISO日時文字列またはISO日付文字列からLocalDateを抽出する
 */
private fun parseDate(dateTimeStr: String): LocalDate? {
    return try {
        LocalDate.parse(dateTimeStr.substringBefore("T"))
    } catch (_: Exception) {
        null
    }
}

/**
 * 背景色の輝度に応じて読みやすいテキスト色（黒/白）を返す。
 * luminance > 0.5 → 黒、それ以下 → 白。
 */
internal fun readableTextColorOn(bg: Color): Color =
    if (bg.luminance() > 0.5f) Color.Black else Color.White

/**
 * HEXカラー文字列（"#7986cb" 等）をComposeのColorに変換する。
 * nullまたはパース不能の場合はnullを返す。
 */
internal fun parseEventColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        val sanitized = hex.removePrefix("#")
        val argb = when (sanitized.length) {
            6 -> (0xFF000000 or sanitized.toLong(16))
            8 -> sanitized.toLong(16)
            else -> return null
        }
        Color(argb)
    } catch (_: Exception) {
        null
    }
}
