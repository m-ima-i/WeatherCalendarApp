package com.anri.weathercalendarapp.common.view.helper

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


fun Long.toDayOfWeek(): String {
    val zoneId = ZoneId.systemDefault()
    val targetDate = Instant.ofEpochSecond(this).atZone(zoneId).toLocalDate()
    val today = LocalDate.now(zoneId)

    return when (targetDate) {
        today -> "今日"
        today.plusDays(1) -> "明日"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())
            targetDate.format(formatter)
        }
    }
}