package com.anri.weathercalendarapp.calendar.domain.model.request

data class CalendarReq(
    val timeMin: String? = null,
    val timeMax: String? = null,
    val orderBy: String? = null,
    val maxResults: Int? = null
)
