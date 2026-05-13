package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.displayTitle
import com.anri.weathercalendarapp.common.datetime.parseEventDate
import com.anri.weathercalendarapp.weather.domain.model.response.Daily
import com.anri.weathercalendarapp.weather.domain.model.response.UpcomingEventWithWeather
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** カレンダー予定に天気情報を紐付け、ホーム画面用の半年以内リストを生成する */
class GetUpcomingEventsWithWeatherUseCase @Inject constructor() {

    operator fun invoke(
        events: List<CalendarEvent>,
        daily: List<Daily>?
    ): List<UpcomingEventWithWeather> {
        val today = LocalDate.now()
        val sixMonthsLater = today.plusMonths(6)
        val dailyByDate = daily?.associateBy { dailyItem ->
            Instant.ofEpochSecond(dailyItem.dt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        } ?: emptyMap()

        return events
            .mapNotNull { event ->
                val eventDate = parseEventDate(event.start) ?: return@mapNotNull null
                if (eventDate.isBefore(today)) return@mapNotNull null
                if (!eventDate.isBefore(sixMonthsLater)) return@mapNotNull null

                val dailyWeather = dailyByDate[eventDate]
                val weatherIcon = dailyWeather?.weather?.firstOrNull()?.icon

                val rawEndDate = parseEventDate(event.end)
                val endDate = when {
                    rawEndDate == null -> null
                    event.isAllDayEvent -> rawEndDate.minusDays(1)
                    else -> rawEndDate
                }
                val isMultiDay = endDate != null && endDate != eventDate

                UpcomingEventWithWeather(
                    eventId = event.id,
                    date = eventDate,
                    endDate = if (isMultiDay) endDate else null,
                    title = event.displayTitle(),
                    startTime = if (!event.isAllDayEvent && !isMultiDay) formatTime(event.start) else null,
                    endTime = if (!event.isAllDayEvent && !isMultiDay) formatTime(event.end) else null,
                    isAllDay = event.isAllDayEvent,
                    weatherIcon = weatherIcon,
                    backgroundColor = event.backgroundColor
                )
            }
            .sortedWith(compareBy({ it.date }, { it.startTime ?: "" }))
    }

    /** ISO 8601 形式の日時文字列から "HH:mm" 部分を抽出する */
    private fun formatTime(dateStr: String?): String? {
        if (dateStr == null) return null
        return try {
            val dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: Exception) {
            null
        }
    }
}
