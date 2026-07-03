package com.anri.weathercalendarapp.widget.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anri.weathercalendarapp.calendar.domain.model.request.CalendarReq
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import com.anri.weathercalendarapp.calendar.domain.usecase.SyncCalendarEventsToLocalUseCase
import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.auth.AuthPreferences
import com.anri.weathercalendarapp.common.auth.GoogleAuthTokenProvider
import com.anri.weathercalendarapp.widget.data.datasource.WidgetLocalDataSource
import com.anri.weathercalendarapp.widget.data.datasource.WidgetUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 日付変更時（00:00）に実行されるWorker。
 * カレンダーAPIから今日以降の最新7件を取得しRoomに保存、Widgetを更新する。
 */
@HiltWorker
class MidnightWidgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val calendarRepository: CalendarRepository,
    private val syncCalendarEventsToLocalUseCase: SyncCalendarEventsToLocalUseCase,
    private val authPreferences: AuthPreferences,
    private val googleAuthTokenProvider: GoogleAuthTokenProvider,
    private val widgetLocalDataSource: WidgetLocalDataSource,
    private val widgetUpdater: WidgetUpdater,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "midnight_widget_update"
        private const val MAX_RESULTS = 7
        private const val RANGE_DAYS = 30L
    }

    override suspend fun doWork(): Result {
        val email = authPreferences.accountEmail.firstOrNull()
        if (email == null) {
            return runWidgetUpdateOnly()
        }

        val token = googleAuthTokenProvider.ensureAccessToken(applicationContext)
        if (token == null) {
            return runWidgetUpdateOnly()
        }

        val timeMin = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val timeMax = LocalDate.now().plusDays(RANGE_DAYS + 1)
            .atStartOfDay(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val req = CalendarReq(
            timeMin = timeMin,
            timeMax = timeMax,
            orderBy = "startTime",
            maxResults = MAX_RESULTS
        )

        return try {
            calendarRepository.getCalendar(req, token).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val events = resource.data?.items ?: emptyList()
                        val sorted = events.distinctBy { it.id }.sortedBy { it.start }
                        syncCalendarEventsToLocalUseCase(sorted)
                    }
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                }
            }

            val opacity = widgetLocalDataSource.getWidgetOpacity().firstOrNull() ?: 100
            widgetUpdater.updateCalendarWidget(opacity)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    /** Email/トークンが揃わない場合でも opacity 同期と Widget 再描画は実施 */
    private suspend fun runWidgetUpdateOnly(): Result {
        return try {
            val opacity = widgetLocalDataSource.getWidgetOpacity().firstOrNull() ?: 100
            widgetUpdater.updateCalendarWidget(opacity)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
