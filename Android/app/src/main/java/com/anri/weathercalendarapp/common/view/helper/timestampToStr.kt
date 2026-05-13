package com.anri.weathercalendarapp.common.view.helper

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toTimeStr(): String {
    val instant = Instant.ofEpochSecond(this)
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return instant.atZone(ZoneId.systemDefault()).format(formatter)
}