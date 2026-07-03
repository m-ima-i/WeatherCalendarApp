package com.anri.weathercalendarapp.calendar.ui.components

import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/** イベント start/end 文字列から LocalDate を抽出 */
internal fun parseEventLocalDate(value: String?): LocalDate? {
    if (value == null) return null
    return try {
        LocalDate.parse(value.substringBefore("T"))
    } catch (_: Exception) {
        null
    }
}

/** イベント start/end 文字列から LocalTime を抽出（時間指定イベント用） */
internal fun parseEventLocalTime(value: String?): LocalTime? {
    if (value == null || !value.contains("T")) return null
    return try {
        OffsetDateTime.parse(value).toLocalTime()
    } catch (_: Exception) {
        try {
            val timePart = value.substringAfter("T").substringBefore("+").substringBefore("-")
            LocalTime.parse(timePart)
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * 多日跨ぎイベントの「(n日目)」サフィックスを算出する。
 * - 単日イベントは null
 * - 終日イベント: end は排他的（例: 3/5〜3/10 の場合 end="3/11"）
 * - 時間指定イベント: end は包括的
 * - displayDate が範囲外の場合は null
 */
internal fun multiDayLabel(event: CalendarEvent, displayDate: LocalDate): String? {
    val startDate = parseEventLocalDate(event.start) ?: return null
    val endDate = parseEventLocalDate(event.end) ?: return null

    val totalDays = if (event.isAllDayEvent) {
        ChronoUnit.DAYS.between(startDate, endDate).toInt()
    } else {
        ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
    }
    if (totalDays <= 1) return null

    val dayIndex = ChronoUnit.DAYS.between(startDate, displayDate).toInt() + 1
    if (dayIndex < 1 || dayIndex > totalDays) return null
    return "（${dayIndex}日目）"
}

/** タイムライン上に配置する時間指定イベントの lane 情報 */
internal data class TimelineEventPlacement(
    val event: CalendarEvent,
    val startMinute: Int,
    val endMinute: Int,
    val lane: Int,
    val laneCount: Int
)

/**
 * 同日内の時間指定イベントを受け取り、各イベントに lane index と
 * 重なるグループ内の総 lane 数を割り当てる。
 *
 * - 表示日の 0:00〜24:00 に投影し、開始・終了の分単位で算出
 * - 表示日をまたぐイベント (start < displayDate or end > displayDate+1) は
 *   表示日の範囲にクリップ
 * - 重なるイベント群を「クラスタ」とし、クラスタ内で全イベントが
 *   同一の laneCount を共有する（Google Calendar 同様の均等分割）
 */
internal fun assignTimelineLanes(
    events: List<CalendarEvent>,
    displayDate: LocalDate
): List<TimelineEventPlacement> {
    if (events.isEmpty()) return emptyList()

    val dayStart = displayDate.atStartOfDay()
    val dayEnd = displayDate.plusDays(1).atStartOfDay()

    data class Item(val event: CalendarEvent, val startMin: Int, val endMin: Int)

    val items = events.mapNotNull { event ->
        val startDate = parseEventLocalDate(event.start) ?: return@mapNotNull null
        val endDate = parseEventLocalDate(event.end) ?: startDate
        val startTime = parseEventLocalTime(event.start) ?: LocalTime.MIDNIGHT
        val endTime = parseEventLocalTime(event.end) ?: LocalTime.MIDNIGHT

        val rawStart = startDate.atTime(startTime)
        val rawEnd = endDate.atTime(endTime)

        val clippedStart = if (rawStart.isBefore(dayStart)) dayStart else rawStart
        val clippedEnd = if (rawEnd.isAfter(dayEnd)) dayEnd else rawEnd

        if (!clippedEnd.isAfter(clippedStart)) return@mapNotNull null

        val startMin = ChronoUnit.MINUTES.between(dayStart, clippedStart).toInt()
            .coerceIn(0, MINUTES_IN_DAY)
        val endMin = ChronoUnit.MINUTES.between(dayStart, clippedEnd).toInt()
            .coerceIn(0, MINUTES_IN_DAY)

        Item(event, startMin, endMin)
    }.sortedWith(compareBy({ it.startMin }, { -it.endMin }))

    if (items.isEmpty()) return emptyList()

    val placements = mutableListOf<TimelineEventPlacement>()
    var clusterEndMin = -1
    var clusterStartIdx = 0
    val laneEnds = mutableListOf<Int>()
    val itemLane = IntArray(items.size)

    fun flushCluster(endIdx: Int) {
        val laneCount = laneEnds.size.coerceAtLeast(1)
        for (i in clusterStartIdx until endIdx) {
            placements.add(
                TimelineEventPlacement(
                    event = items[i].event,
                    startMinute = items[i].startMin,
                    endMinute = items[i].endMin,
                    lane = itemLane[i],
                    laneCount = laneCount
                )
            )
        }
    }

    items.forEachIndexed { idx, item ->
        if (item.startMin >= clusterEndMin) {
            flushCluster(idx)
            clusterStartIdx = idx
            laneEnds.clear()
            clusterEndMin = item.endMin
        }
        val laneIdx = laneEnds.indexOfFirst { it <= item.startMin }
        if (laneIdx >= 0) {
            laneEnds[laneIdx] = item.endMin
            itemLane[idx] = laneIdx
        } else {
            itemLane[idx] = laneEnds.size
            laneEnds.add(item.endMin)
        }
        if (item.endMin > clusterEndMin) clusterEndMin = item.endMin
    }
    flushCluster(items.size)
    return placements
}

internal const val MINUTES_IN_DAY: Int = 24 * 60
