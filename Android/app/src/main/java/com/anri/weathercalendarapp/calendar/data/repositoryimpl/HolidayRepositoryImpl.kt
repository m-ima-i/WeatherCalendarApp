package com.anri.weathercalendarapp.calendar.data.repositoryimpl

import com.anri.weathercalendarapp.calendar.data.datasource.HolidayRemoteDataSource
import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.repository.HolidayRepository
import javax.inject.Inject

class HolidayRepositoryImpl @Inject constructor(
    private val remoteDataSource: HolidayRemoteDataSource
) : HolidayRepository {

    override suspend fun getHolidays(
        startYearMonth: String,
        endYearMonth: String,
        authToken: String
    ): List<HolidayEvent> {
        val timeMin = "${startYearMonth}-01T00:00:00Z"

        val endParts = endYearMonth.split("-")
        val endYear = endParts[0].toInt()
        val endMonth = endParts[1].toInt()
        val nextMonth = if (endMonth == 12) "${endYear + 1}-01" else "${endYear}-${(endMonth + 1).toString().padStart(2, '0')}"
        val timeMax = "${nextMonth}-01T00:00:00Z"

        return remoteDataSource.getHolidays(
            timeMin = timeMin,
            timeMax = timeMax,
            authToken = authToken
        )
    }
}
