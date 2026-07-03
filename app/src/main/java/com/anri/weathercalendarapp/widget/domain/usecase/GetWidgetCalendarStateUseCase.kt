package com.anri.weathercalendarapp.widget.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.common.datetime.parseEventDate
import com.anri.weathercalendarapp.widget.domain.model.WidgetCalendarState
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/** ウィジェット用のカレンダー表示状態を生成する（未連携/予定なし/予定あり） */
class GetWidgetCalendarStateUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val authPreferences: AuthPreferences
) {
    suspend operator fun invoke(): WidgetCalendarState {
        // Google 未連携時は予定の取得自体が失敗扱い
        if (authPreferences.accountEmail.first() == null) {
            return WidgetCalendarState.NotAuthorized
        }

        val events = calendarRepository.getLocalEventsOnce()
        val today = LocalDate.now()
        val upcoming = events.filter { event ->
            val date = parseEventDate(event.start) ?: return@filter false
            !date.isBefore(today)
        }
        return if (upcoming.isEmpty()) {
            WidgetCalendarState.NoEvents
        } else {
            WidgetCalendarState.HasEvents(upcoming)
        }
    }
}
