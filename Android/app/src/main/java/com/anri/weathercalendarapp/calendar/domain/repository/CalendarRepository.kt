package com.anri.weathercalendarapp.calendar.domain.repository

import com.anri.weathercalendarapp.calendar.domain.model.request.CalendarReq
import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarRes
import com.anri.weathercalendarapp.common.Resource
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {

    /** Google Calendar APIから指定期間の予定を取得する */
    fun getCalendar(req: CalendarReq, authToken: String): Flow<Resource<CalendarRes>>

    /** 予定を作成 */
    suspend fun createEvent(
        authToken: String,
        body: CreateEventReq,
        calendarId: String = "primary"
    ): Result<CalendarEvent>

    /** 予定を更新 */
    suspend fun updateEvent(
        authToken: String,
        eventId: String,
        body: CreateEventReq,
        calendarId: String = "primary"
    ): Result<CalendarEvent>

    /** 予定を削除 */
    suspend fun deleteEvent(
        authToken: String,
        eventId: String,
        calendarId: String = "primary"
    ): Result<Unit>

    /** ウィジェット用: ローカルの予定を全件削除 */
    suspend fun deleteAllLocalEvents()

    /** ウィジェット用: ローカルの予定を更新 */
    suspend fun syncEventsToLocal(events: List<CalendarEvent>)

    /** ウィジェット用: ローカルに保存された全予定を取得 */
    suspend fun getLocalEventsOnce(): List<CalendarEvent>
}
