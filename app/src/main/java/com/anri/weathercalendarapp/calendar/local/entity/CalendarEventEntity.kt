package com.anri.weathercalendarapp.calendar.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val summary: String,
    val start: String?,
    val end: String?,
    val isAllDayEvent: Boolean,
    val colorId: String?,
    val backgroundColor: String?
)

fun CalendarEventEntity.toDomain(): CalendarEvent {
    return CalendarEvent(
        id = id,
        summary = summary,
        start = start,
        end = end,
        isAllDayEvent = isAllDayEvent,
        colorId = colorId,
        backgroundColor = backgroundColor
    )
}

fun CalendarEvent.toEntity(): CalendarEventEntity {
    return CalendarEventEntity(
        id = id,
        summary = summary,
        start = start,
        end = end,
        isAllDayEvent = isAllDayEvent,
        colorId = colorId,
        backgroundColor = backgroundColor
    )
}
