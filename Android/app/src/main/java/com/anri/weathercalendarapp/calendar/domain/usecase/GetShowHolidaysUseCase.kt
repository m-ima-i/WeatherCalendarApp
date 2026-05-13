package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.repository.CalendarPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 祝日表示設定をDataStoreからFlowで取得する */
class GetShowHolidaysUseCase @Inject constructor(
    private val repository: CalendarPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.getShowHolidays()
}
