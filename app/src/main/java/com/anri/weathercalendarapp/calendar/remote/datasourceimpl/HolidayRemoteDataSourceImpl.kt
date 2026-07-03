package com.anri.weathercalendarapp.calendar.remote.datasourceimpl

import com.anri.weathercalendarapp.calendar.data.datasource.HolidayRemoteDataSource
import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.remote.apiinterface.CalendarApiService
import java.time.LocalDate
import javax.inject.Inject

class HolidayRemoteDataSourceImpl @Inject constructor(
    private val apiService: CalendarApiService
) : HolidayRemoteDataSource {

    override suspend fun getHolidays(
        timeMin: String,
        timeMax: String,
        authToken: String
    ): List<HolidayEvent> {
        val authHeader = "Bearer $authToken"
        val response = apiService.getEvents(
            authHeader = authHeader,
            calendarId = CALENDAR_ID_JP,
            timeMin = timeMin,
            timeMax = timeMax,
            singleEvents = true,
            orderBy = "startTime"
        )

        return response.items?.mapNotNull { event ->
            val dateStr = event.start?.date ?: return@mapNotNull null
            val date = try {
                LocalDate.parse(dateStr)
            } catch (_: Exception) {
                return@mapNotNull null
            }
            HolidayEvent(
                date = date,
                name = event.summary ?: "(祝日名なし)"
            )
        } ?: emptyList()
    }

    companion object {
        private const val CALENDAR_ID_JP = "ja.japanese#holiday@group.v.calendar.google.com"
    }
}
