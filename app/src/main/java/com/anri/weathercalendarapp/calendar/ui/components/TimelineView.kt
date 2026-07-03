package com.anri.weathercalendarapp.calendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.displayTitle
import java.time.LocalDate
import java.time.LocalTime

private val HourHeight = 40.dp
private val LabelWidth = 30.dp
// EventBlock 幅を AllDayBar 幅と揃えるための inset
// 左 = 日付Col 占有幅(5+30+5=40)
private val EventAreaStartInset = 40.dp
// 右 = AllDayHeader Row outer horizontal padding なし
private val EventAreaEndInset = 0.dp

/**
 * 0:00〜24:00 の 24時間タイムライン。
 * - 縦スクロール、初期位置は 0:00（rememberScrollState のデフォルト=0）
 * - 各時刻 Row 全体（予定の色帯部分以外）をタップすると onAddEventAt(該当時刻)
 * - 既存予定の色帯タップで onEventClick(event)
 * - 重なる予定は assignTimelineLanes により lane 均等分割
 */
@Composable
fun TimelineView(
    date: LocalDate,
    events: List<CalendarEvent>,
    onAddEventAt: (LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val placements = remember(events, date) { assignTimelineLanes(events, date) }
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(HourHeight * 24)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (hour in 0..23) {
                    HourRow(
                        hour = hour,
                        onClick = { onAddEventAt(LocalTime.of(hour, 0)) }
                    )
                }
            }

            // 予定の色帯オーバーレイ（絶対配置、AllDayBar と幅を揃えるため inset）
            val eventAreaWidth = maxWidth - EventAreaStartInset - EventAreaEndInset
            placements.forEach { placement ->
                val topDp = HourHeight * (placement.startMinute / 60f)
                val durationMinutes = (placement.endMinute - placement.startMinute).toFloat()
                val heightDp = (HourHeight * (durationMinutes / 60f) - 1.dp).coerceAtLeast(24.dp)
                val laneWidthDp = eventAreaWidth / placement.laneCount
                val xDp = EventAreaStartInset + laneWidthDp * placement.lane
                val color = parseEventColor(placement.event.backgroundColor) ?: primaryColor

                EventBlock(
                    title = placement.event.displayTitle(),
                    color = color,
                    onClick = { onEventClick(placement.event) },
                    modifier = Modifier
                        .offset(x = xDp, y = topDp)
                        .width(laneWidthDp)
                        .height(heightDp)
                        .padding(horizontal = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun HourRow(
    hour: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HourHeight),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .width(LabelWidth)
                .height(HourHeight)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$hour:00",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            if (hour != 0) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun EventBlock(
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(verticalArrangement = Arrangement.Top) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = readableTextColorOn(color),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
