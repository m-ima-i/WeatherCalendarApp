package com.anri.weathercalendarapp.calendar.remote.datasourceimpl

import com.anri.weathercalendarapp.calendar.data.datasource.CalendarRemoteDataSource
import com.anri.weathercalendarapp.calendar.domain.model.request.CalendarReq
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarRes
import com.anri.weathercalendarapp.calendar.remote.apiinterface.CalendarApiService
import com.anri.weathercalendarapp.calendar.remote.model.request.toRemote
import com.anri.weathercalendarapp.calendar.remote.model.response.toDomain
import javax.inject.Inject

class CalendarRemoteDataSourceImpl @Inject constructor(
    private val apiService: CalendarApiService
): CalendarRemoteDataSource {

    override suspend fun getCalendar(req: CalendarReq, authToken: String): CalendarRes {
        val request = req.toRemote()
        val authHeader = "Bearer $authToken"

        // ページネーションで全件取得（maxResults 指定時は到達したら早期 break）
        val allEvents = mutableListOf<CalendarEvent>()
        var pageToken: String? = null
        do {
            val remote = apiService.getEvents(
                authHeader = authHeader,
                calendarId = request.calendarId,
                timeMin = request.timeMin,
                timeMax = request.timeMax,
                singleEvents = request.singleEvents,
                orderBy = request.orderBy,
                maxResults = request.maxResults,
                pageToken = pageToken
            )
            allEvents.addAll(remote.toDomain().items)
            pageToken = remote.nextPageToken
            if (request.maxResults != null && allEvents.size >= request.maxResults) {
                break
            }
        } while (pageToken != null)

        return CalendarRes(
            items = allEvents
        )
    }

    override suspend fun createEvent(
        authToken: String,
        body: CreateEventReq,
        calendarId: String
    ): CalendarEvent {
        val authHeader = "Bearer $authToken"
        val remote = apiService.createEvent(
            authHeader = authHeader,
            calendarId = calendarId,
            body = body.toRemote()
        )
        return remote.toDomain() ?: throw IllegalStateException("作成されたイベントの変換に失敗しました")
    }

    override suspend fun updateEvent(
        authToken: String,
        eventId: String,
        body: CreateEventReq,
        calendarId: String
    ): CalendarEvent {
        val authHeader = "Bearer $authToken"
        val remote = apiService.updateEvent(
            authHeader = authHeader,
            calendarId = calendarId,
            eventId = eventId,
            body = body.toRemote()
        )
        return remote.toDomain() ?: throw IllegalStateException("更新されたイベントの変換に失敗しました")
    }

    override suspend fun deleteEvent(
        authToken: String,
        eventId: String,
        calendarId: String
    ) {
        val authHeader = "Bearer $authToken"
        apiService.deleteEvent(
            authHeader = authHeader,
            calendarId = calendarId,
            eventId = eventId
        )
    }
}
