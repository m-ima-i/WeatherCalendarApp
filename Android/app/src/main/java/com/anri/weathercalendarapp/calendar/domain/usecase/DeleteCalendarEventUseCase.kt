package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import javax.inject.Inject

/** Google Calendar APIで予定を削除する */
class DeleteCalendarEventUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(
        authToken: String,
        eventId: String
    ): Result<Unit> {
        return calendarRepository.deleteEvent(authToken, eventId)
    }
}
