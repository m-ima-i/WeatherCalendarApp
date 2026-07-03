package com.anri.weathercalendarapp.calendar.domain.model.request

import java.time.LocalDate
import java.time.LocalTime

/**
 * 予定作成用のDomain Model
 */
data class CreateEventReq(
    val summary: String? = null,
    val isAllDay: Boolean,
    val startDate: LocalDate,
    val startTime: LocalTime? = null,
    val endDate: LocalDate? = null,
    val endTime: LocalTime? = null,
    val colorId: String? = null
)
