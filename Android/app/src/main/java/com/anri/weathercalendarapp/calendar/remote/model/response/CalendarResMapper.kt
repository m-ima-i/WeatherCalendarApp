package com.anri.weathercalendarapp.calendar.remote.model.response

import com.anri.weathercalendarapp.calendar.domain.model.EventColors
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarRes

fun EventRemote.toDomain(): CalendarEvent? {
    val eventId = id ?: return null
    val isAllDay = start?.date != null

    val startStr = if (isAllDay) start?.date else start?.dateTime
    val endStr = if (isAllDay) end?.date else end?.dateTime

    return CalendarEvent(
        id = eventId,
        summary = summary.orEmpty(),
        start = startStr,
        end = endStr,
        isAllDayEvent = isAllDay,
        colorId = colorId,
        backgroundColor = EventColors.getBackground(colorId)
    )
}

fun CalendarResRemote.toDomain(): CalendarRes {
    return CalendarRes(
        items = items?.mapNotNull { it.toDomain() } ?: emptyList()
    )
}
