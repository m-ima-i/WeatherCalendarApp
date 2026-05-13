package com.anri.weathercalendarapp.common.datetime

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Google Calendar API が返す日付文字列を [LocalDate] に変換する。
 *
 * 受理する形式:
 * - ISO date 形式 `YYYY-MM-DD`（終日イベント、例: `2024-01-15`）
 * - ISO date-time 形式 `YYYY-MM-DDTHH:MM:SS±HH:MM`（時間指定イベント、例: `2024-01-15T10:00:00+09:00`）
 *
 * タイムゾーン変換は行わず、ローカル日付部分をそのまま返す。
 */
fun parseEventDate(value: String?): LocalDate? {
    if (value == null) return null
    return try {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: Exception) {
        try {
            OffsetDateTime.parse(value).toLocalDate()
        } catch (_: Exception) {
            null
        }
    }
}
