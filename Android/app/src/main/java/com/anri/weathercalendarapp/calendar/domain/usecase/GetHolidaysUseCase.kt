package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.model.HolidayEvent
import com.anri.weathercalendarapp.calendar.domain.repository.HolidayRepository
import javax.inject.Inject

/** 指定範囲の祝日をAPIから取得する */
class GetHolidaysUseCase @Inject constructor(
    private val repository: HolidayRepository
) {
    suspend operator fun invoke(
        startYearMonth: String,
        endYearMonth: String,
        authToken: String
    ): List<HolidayEvent> {
        return repository.getHolidays(startYearMonth, endYearMonth, authToken)
    }
}
