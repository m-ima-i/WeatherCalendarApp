package com.anri.weathercalendarapp.calendar.data.repositoryimpl

import com.anri.weathercalendarapp.calendar.data.datasource.CalendarPreferencesDataSource
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CalendarPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: CalendarPreferencesDataSource
) : CalendarPreferencesRepository {
    override fun getShowHolidays(): Flow<Boolean> = dataSource.getShowHolidays()
    override suspend fun setShowHolidays(value: Boolean) = dataSource.setShowHolidays(value)
}
