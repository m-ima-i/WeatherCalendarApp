package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import javax.inject.Inject

/** Google Calendar APIで既存の予定を更新する */
class UpdateCalendarEventUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(
        authToken: String,
        eventId: String,
        body: CreateEventReq
    ): Result<CalendarEvent> {
        return calendarRepository.updateEvent(authToken, eventId, body)
    }
}
