package com.anri.weathercalendarapp.calendar.data.datasource

import com.anri.weathercalendarapp.calendar.domain.model.request.CalendarReq
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarRes

interface CalendarRemoteDataSource {

    /** Google Calendar APIから指定期間の予定を取得する */
    suspend fun getCalendar(req: CalendarReq, authToken: String): CalendarRes

    /** Google Calendar APIで新しい予定を作成する */
    suspend fun createEvent(
        authToken: String,
        body: CreateEventReq,
        calendarId: String = "primary"
    ): CalendarEvent

    /** Google Calendar APIで既存の予定を更新する */
    suspend fun updateEvent(
        authToken: String,
        eventId: String,
        body: CreateEventReq,
        calendarId: String = "primary"
    ): CalendarEvent

    /** Google Calendar APIで予定を削除する */
    suspend fun deleteEvent(
        authToken: String,
        eventId: String,
        calendarId: String = "primary"
    )
}