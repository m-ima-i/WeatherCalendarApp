package com.anri.weathercalendarapp.calendar.domain.model.response

data class CalendarRes(
    val items: List<CalendarEvent>
)

data class CalendarEvent(
    val id: String,
    val summary: String,
    val start: String?,
    val end: String?,
    val isAllDayEvent: Boolean,
    val colorId: String? = null,
    val backgroundColor: String? = null
)

fun CalendarEvent.displayTitle(): String = summary.ifBlank { "(タイトルなし)" }
