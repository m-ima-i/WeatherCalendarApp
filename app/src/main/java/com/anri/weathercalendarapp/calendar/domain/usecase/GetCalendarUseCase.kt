package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.model.request.CalendarReq
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarRes
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import com.anri.weathercalendarapp.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Google Calendar APIから指定期間の予定を取得する */
class GetCalendarUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    operator fun invoke(
        req: CalendarReq,
        authToken: String
    ): Flow<Resource<CalendarRes>> {
        return calendarRepository.getCalendar(req, authToken)
    }
}