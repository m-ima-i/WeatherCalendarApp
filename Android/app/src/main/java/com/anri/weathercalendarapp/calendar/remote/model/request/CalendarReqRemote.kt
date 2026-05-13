package com.anri.weathercalendarapp.calendar.remote.model.request

import com.anri.weathercalendarapp.calendar.domain.model.request.CalendarReq

data class CalendarReqRemote(
    val calendarId: String = "primary",
    val timeMin: String? = null,
    val timeMax: String? = null,
    val singleEvents: Boolean = true,
    val orderBy: String? = null,
    val maxResults: Int? = null
)

fun CalendarReq.toRemote(): CalendarReqRemote = CalendarReqRemote(
    timeMin = timeMin,
    timeMax = timeMax,
    orderBy = orderBy,
    maxResults = maxResults
)
