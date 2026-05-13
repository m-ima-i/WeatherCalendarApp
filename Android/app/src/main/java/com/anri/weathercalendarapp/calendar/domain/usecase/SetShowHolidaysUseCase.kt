package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.repository.CalendarPreferencesRepository
import javax.inject.Inject

/** 祝日表示設定をDataStoreに保存する */
class SetShowHolidaysUseCase @Inject constructor(
    private val repository: CalendarPreferencesRepository
) {
    suspend operator fun invoke(value: Boolean) = repository.setShowHolidays(value)
}
