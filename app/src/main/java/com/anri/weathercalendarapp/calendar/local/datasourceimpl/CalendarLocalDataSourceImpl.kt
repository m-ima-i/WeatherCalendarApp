package com.anri.weathercalendarapp.calendar.local.datasourceimpl

import com.anri.weathercalendarapp.calendar.data.datasource.CalendarLocalDataSource
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.local.dao.CalendarEventDao
import com.anri.weathercalendarapp.calendar.local.entity.toDomain
import com.anri.weathercalendarapp.calendar.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalendarLocalDataSourceImpl @Inject constructor(
    private val calendarEventDao: CalendarEventDao
) : CalendarLocalDataSource {

    override suspend fun deleteAll() {
        calendarEventDao.deleteAll()
    }

    override suspend fun replaceAll(events: List<CalendarEvent>) {
        calendarEventDao.replaceAll(events.map { it.toEntity() })
    }

    override suspend fun getAllOnce(): List<CalendarEvent> {
        return calendarEventDao.getAllOnce().map { it.toDomain() }
    }
}
