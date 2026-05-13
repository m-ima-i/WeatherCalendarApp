package com.anri.weathercalendarapp.calendar.domain.usecase

import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import com.anri.weathercalendarapp.common.datetime.parseEventDate
import java.time.LocalDate
import javax.inject.Inject

/**
 * APIから取得した全予定から「今日から30日以内・最新7件」を計算し、Local に毎回保存する。
 * Local 反映 + Widget 再描画を実行する
 */
class SyncCalendarEventsToLocalUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    companion object {
        private const val LOCAL_STOCK_LIMIT = 7
        private const val RANGE_DAYS = 30L
    }

    suspend operator fun invoke(allEvents: List<CalendarEvent>) {
        val today = LocalDate.now()
        val rangeEnd = today.plusDays(RANGE_DAYS)

        val newEvents = allEvents
            .filter { event ->
                val startDate = parseEventDate(event.start) ?: return@filter false
                !startDate.isBefore(today) && !startDate.isAfter(rangeEnd)
            }
            .distinctBy { it.id }
            .sortedBy { it.start }
            .take(LOCAL_STOCK_LIMIT)

        calendarRepository.syncEventsToLocal(newEvents)
    }
}
