package com.anri.weathercalendarapp.calendar.data.datasource

import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent

interface HolidayRemoteDataSource {
    /** 日本祝日カレンダーから祝日イベントを取得する */
    suspend fun getHolidays(
        timeMin: String,
        timeMax: String,
        authToken: String
    ): List<HolidayEvent>
}
