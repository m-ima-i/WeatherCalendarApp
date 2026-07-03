package com.anri.weathercalendarapp.calendar.remote.model.request

import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun CreateEventReq.toRemote(): CreateEventReqRemote {
    val timezone = ZoneId.systemDefault().id

    val startRemote: EventDateTimeRemote
    val endRemote: EventDateTimeRemote

    if (isAllDay) {
        startRemote = EventDateTimeRemote(date = startDate.toString())
        // 終日イベントのendは排他的（翌日を指定）
        val end = endDate?.plusDays(1) ?: startDate.plusDays(1)
        endRemote = EventDateTimeRemote(date = end.toString())
    } else {
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val startZdt = startDate.atTime(startTime ?: LocalTime.of(9, 0))
            .atZone(ZoneId.systemDefault())
        val endZdt = (endDate ?: startDate).atTime(endTime ?: startTime?.plusHours(1) ?: LocalTime.of(10, 0))
            .atZone(ZoneId.systemDefault())
        startRemote = EventDateTimeRemote(dateTime = startZdt.format(formatter), timeZone = timezone)
        endRemote = EventDateTimeRemote(dateTime = endZdt.format(formatter), timeZone = timezone)
    }

    return CreateEventReqRemote(
        summary = summary?.takeIf { it.isNotBlank() },
        start = startRemote,
        end = endRemote,
        colorId = colorId
    )
}
