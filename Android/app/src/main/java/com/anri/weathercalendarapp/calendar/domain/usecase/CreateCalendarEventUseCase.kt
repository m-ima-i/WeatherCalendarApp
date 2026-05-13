package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.model.request.CreateEventReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import javax.inject.Inject

/** Google Calendar APIで新しい予定を作成する */
class CreateCalendarEventUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(
        authToken: String,
        body: CreateEventReq
    ): Result<CalendarEvent> {
        return calendarRepository.createEvent(authToken, body)
    }
}
