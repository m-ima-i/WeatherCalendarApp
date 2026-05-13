package com.anri.weathercalendarapp.calendar.domain.repository

import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent

interface HolidayRepository {
    /** 指定範囲日本の祝日をGoogle Calendar APIから取得する */
    suspend fun getHolidays(startYearMonth: String, endYearMonth: String, authToken: String): List<HolidayEvent>
}
