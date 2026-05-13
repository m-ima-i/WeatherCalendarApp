package com.anri.weathercalendarapp.calendar.data.repositoryimpl

import com.anri.weathercalendarapp.calendar.data.datasource.CalendarLocalDataSource
import com.anri.weathercalendarapp.calendar.data.datasource.CalendarRemoteDataSource
import com.anri.weathercalendarapp.calendar.domain.model.request.CalendarReq
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarRes
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.widget.data.datasource.WidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val calendarRemoteDataSource: CalendarRemoteDataSource,
    private val calendarLocalDataSource: CalendarLocalDataSource,
    private val widgetUpdater: WidgetUpdater,
) : CalendarRepository {

    override fun getCalendar(
        req: CalendarReq,
        authToken: String
    ): Flow<Resource<CalendarRes>> = flow {
        emit(Resource.Loading())

        try {
            val responseData = calendarRemoteDataSource.getCalendar(req, authToken)
            emit(Resource.Success(responseData))
        } catch (e: Exception) {
            emit(Resource.Error(cause = e))
        }
    }

    override suspend fun createEvent(
        authToken: String,
        body: CreateEventReq,
        calendarId: String
    ): Result<CalendarEvent> {
        return try {
            val event = calendarRemoteDataSource.createEvent(authToken, body, calendarId)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEvent(
        authToken: String,
        eventId: String,
        body: CreateEventReq,
        calendarId: String
    ): Result<CalendarEvent> {
        return try {
            val event = calendarRemoteDataSource.updateEvent(authToken, eventId, body, calendarId)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(
        authToken: String,
        eventId: String,
        calendarId: String
    ): Result<Unit> {
        return try {
            calendarRemoteDataSource.deleteEvent(authToken, eventId, calendarId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllLocalEvents() {
        calendarLocalDataSource.deleteAll()
        // ログアウト等で予定をクリアした際も Widget を再描画する（NotAuthorized 状態を反映）
        widgetUpdater.refreshCalendarWidget()
    }

    override suspend fun syncEventsToLocal(events: List<CalendarEvent>) {
        calendarLocalDataSource.replaceAll(events)
        widgetUpdater.refreshCalendarWidget()
    }

    override suspend fun getLocalEventsOnce(): List<CalendarEvent> {
        return calendarLocalDataSource.getAllOnce()
    }

}
