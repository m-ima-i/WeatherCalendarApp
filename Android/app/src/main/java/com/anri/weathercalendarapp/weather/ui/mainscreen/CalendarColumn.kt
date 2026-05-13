package com.anri.weathercalendarapp.weather.ui.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.graphics.Color as AndroidColor
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.calendar.presentation.type.CalendarFailureType
import com.anri.weathercalendarapp.calendar.ui.common.CalendarFailureContent
import com.anri.weathercalendarapp.common.view.CustomElevateCared
import com.anri.weathercalendarapp.weather.domain.model.response.UpcomingEventWithWeather
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.weather.ui.graphics.WeatherAnimation
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarColumn(
    modifier: Modifier = Modifier,
    upcomingEvents: List<UpcomingEventWithWeather>? = null,
    isCalendarInitialized: Boolean = false,
    isCalendarAuthorized: Boolean = false,
    failureType: CalendarFailureType? = null,
    isLoading: Boolean = false,
    onRequestLogin: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    CustomElevateCared(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.upcoming_events_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (failureType != null) {
                // 失敗状態（API_UNAUTHORIZED は再連携ボタン付き）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    CalendarFailureContent(
                        failureType = failureType,
                        isLoading = isLoading,
                        onRequestReauth = onRequestLogin,
                        onRefresh = onRefresh
                    )
                }
            } else if (isCalendarInitialized && !isCalendarAuthorized) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.calendar_login_prompt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRequestLogin) {
                            Text(text = stringResource(R.string.calendar_login_button))
                        }
                    }
                }
            } else if (upcomingEvents == null) {
                // 未取得: ローダー表示（events が一度も成功取得されていない）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (upcomingEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_upcoming_events),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 年月でグルーピング
                val grouped = remember(upcomingEvents) {
                    upcomingEvents.groupBy { YearMonth.from(it.date) }
                }

                val listItems = remember(grouped) {
                    buildList {
                        var prevYear: Int? = null
                        grouped.entries.forEachIndexed { groupIndex, (yearMonth, events) ->
                            if (groupIndex > 0) {
                                add(CalendarListItem.GroupSpacer(groupIndex))
                            }
                            val showYear = prevYear != yearMonth.year
                            add(CalendarListItem.Header(yearMonth, showYear))
                            events.forEachIndexed { eventIndex, event ->
                                add(CalendarListItem.Event(event, "${groupIndex}_${eventIndex}"))
                            }
                            prevYear = yearMonth.year
                        }
                    }
                }

                val lazyListState = rememberLazyListState()
                val density = LocalDensity.current

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = listItems,
                        key = { item ->
                            when (item) {
                                is CalendarListItem.Header -> "header_${item.yearMonth}"
                                is CalendarListItem.Event -> "event_${item.uniqueKey}"
                                is CalendarListItem.GroupSpacer -> "spacer_${item.index}"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is CalendarListItem.GroupSpacer -> {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            is CalendarListItem.Header -> {
                                Column {
                                    if (item.showYear) {
                                        Text(
                                            text = "${item.yearMonth.year}年",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${item.yearMonth.monthValue}月",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            is CalendarListItem.Event -> {
                                UpcomingEventRow(
                                    event = item.event,
                                    onClick = { onEventClick(item.event.eventId) }
                                )
                            }
                        }
                    }

                    item(key = "footer") {
                        // フッター以外のアイテムが占める高さを計算
                        val info = lazyListState.layoutInfo
                        val contentHeight = info.visibleItemsInfo
                            .filter { it.key != "footer" }
                            .sumOf { it.size }
                        val spacingPx = with(density) { 4.dp.roundToPx() }
                        val itemCount = info.visibleItemsInfo.count { it.key != "footer" }
                        val totalContentHeight = contentHeight + (itemCount - 1).coerceAtLeast(0) * spacingPx
                        val viewportHeight = info.viewportEndOffset - info.viewportStartOffset
                        val remainingPx = viewportHeight - totalContentHeight - spacingPx

                        if (remainingPx > with(density) { 48.dp.roundToPx() }) {
                            // 余白がある場合: 残りスペースの中央にテキスト表示
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(with(density) { remainingPx.toDp() }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "半年以内の予定を表示しています",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // スクロールが必要な場合: 末尾にテキスト表示
                            Text(
                                text = "半年以内の予定を表示しています",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed class CalendarListItem {
    data class Header(val yearMonth: YearMonth, val showYear: Boolean) : CalendarListItem()
    data class Event(val event: UpcomingEventWithWeather, val uniqueKey: String) : CalendarListItem()
    data class GroupSpacer(val index: Int) : CalendarListItem()
}

@Composable
private fun UpcomingEventRow(
    event: UpcomingEventWithWeather,
    onClick: () -> Unit
) {
    val dayStr = "${event.date.dayOfMonth}日"
    val dateFormatter = DateTimeFormatter.ofPattern("M/d")

    // 開始・波線・終了を分離（波線の垂直位置を揃えるため）
    val startStr: String?
    val endStr: String?
    if (event.endDate != null) {
        startStr = event.date.format(dateFormatter)
        endStr = event.endDate.format(dateFormatter)
    } else if (event.isAllDay) {
        startStr = null
        endStr = null
    } else {
        startStr = event.startTime
        endStr = event.endTime
    }

    val weatherType = WeatherType.fromId(event.weatherIcon)

    // カレンダー色バー（パース失敗時は primaryContainer フォールバック / ウィジェットと同仕様）
    val barColor = event.backgroundColor?.let {
        try { Color(AndroidColor.parseColor(it)) } catch (_: Exception) { null }
    } ?: MaterialTheme.colorScheme.primaryContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = dayStr,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = event.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        if (weatherType != null) {
            Spacer(modifier = Modifier.width(8.dp))
            WeatherAnimation(
                type = weatherType.toAnimationType(),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        if (startStr != null && endStr != null) {
            Text(
                text = startStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
            Text(
                text = " ~ ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = endStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
        } else if (event.isAllDay) {
            Text(
                text = "終日",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
