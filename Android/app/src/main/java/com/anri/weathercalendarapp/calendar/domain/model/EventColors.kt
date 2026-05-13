package com.anri.weathercalendarapp.calendar.domain.model

/**
 * Google Calendar APIのイベントカラーパレット（固定値）
 * https://developers.google.com/calendar/api/v3/reference/colors
 */
object EventColors {
    /** colorId → backgroundColor */
    val PALETTE: Map<String, String> = mapOf(
        "1" to "#7986cb",   // Lavender
        "2" to "#33b679",   // Sage
        "3" to "#8e24aa",   // Grape
        "4" to "#e67c73",   // Flamingo
        "5" to "#f6bf26",   // Banana
        "6" to "#f4511e",   // Tangerine
        "7" to "#039be5",   // Peacock
        "8" to "#616161",   // Graphite
        "9" to "#3f51b5",   // Blueberry
        "10" to "#0b8043",  // Basil
        "11" to "#d50000"   // Tomato
    )

    fun getBackground(colorId: String?): String? = colorId?.let { PALETTE[it] }
}
