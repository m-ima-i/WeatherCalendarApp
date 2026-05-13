package com.anri.weathercalendarapp.common.view.helper

import kotlin.math.roundToInt

object TemperatureFormatter {

    fun formatTemperature(celsius: Double): String {
        return "${celsius.roundToInt()}°C"
    }

    fun formatTemperatureValue(celsius: Double): String {
        return "${celsius.roundToInt()}°"
    }
}
