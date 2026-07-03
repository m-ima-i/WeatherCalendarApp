package com.anri.weathercalendarapp.weather.domain.model.response

import java.time.LocalDate

data class UpcomingEventWithWeather(
    val eventId: String,
    val date: LocalDate,
    val endDate: LocalDate?,
    val title: String,
    val startTime: String?,
    val endTime: String?,
    val isAllDay: Boolean,
    val weatherIcon: String?,
    val backgroundColor: String? = null
)
