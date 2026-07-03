package com.anri.weathercalendarapp.calendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.displayTitle
import java.time.LocalDate

private val AllDayBarHeight = 20.dp
private val AllDayBarSpacing = 4.dp
private val AllDayMinHeight = 40.dp

/**
 * 日付画面オーバーレイの上部ヘッダー。
 * - 左: 大きな日番号 + 曜日
 * - 右: 終日予定（祝日含む）の色帯リスト（2件までは固定高、3件以上で伸びる）
 * - 各色帯タップで EditEventDialog を開く（祝日はタップ無効）
 * - 右カラムの余白タップで onAddAllDay 発火（終日予定の追加）
 */
@Composable
fun AllDayHeader(
    date: LocalDate,
    allDayEvents: List<CalendarEvent>,
    holidays: List<HolidayEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onAddAllDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfWeekNames = listOf("日", "月", "火", "水", "木", "金", "土")
    val dayName = dayOfWeekNames[date.dayOfWeek.value % 7]
    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左: 日番号 + 曜日（タップで終日予定追加）
        Column(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .width(30.dp)
                .clickable(onClick = onAddAllDay),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 右: 終日予定リスト（余白タップで終日予定追加）
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = AllDayMinHeight)
                .clickable(onClick = onAddAllDay),
            verticalArrangement = Arrangement.spacedBy(AllDayBarSpacing)
        ) {
            holidays.forEach { holiday ->
                AllDayBar(
                    title = holiday.name,
                    color = errorColor,
                    onClick = null
                )
            }
            allDayEvents.forEach { event ->
                val color = parseEventColor(event.backgroundColor) ?: primaryColor
                val suffix = multiDayLabel(event, date) ?: ""
                AllDayBar(
                    title = event.displayTitle() + suffix,
                    color = color,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}

@Composable
private fun AllDayBar(
    title: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)?
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .height(AllDayBarHeight)
        .clip(RoundedCornerShape(8.dp))
        .background(color)
    val withClick = if (onClick != null) baseModifier.clickable(onClick = onClick) else baseModifier
    Box(
        modifier = withClick.padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = readableTextColorOn(color),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start
        )
    }
}
