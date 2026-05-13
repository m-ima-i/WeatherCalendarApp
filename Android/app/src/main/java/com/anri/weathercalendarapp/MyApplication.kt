package com.anri.weathercalendarapp

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anri.weathercalendarapp.common.AppInfo
import com.anri.weathercalendarapp.weather.data.worker.WeatherWorker
import com.anri.weathercalendarapp.widget.data.worker.MidnightWidgetWorker
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // WorkerFactoryの設定
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupWeatherWork()
        setupMidnightWidgetWork()
        if(!Places.isInitialized()) Places.initializeWithNewPlacesApiEnabled(applicationContext, AppInfo.PLACES_API_KEY)
    }

    /** 定期的に天気をAPI取得しRoomに保存するWorkerをスケジュール */
    private fun setupWeatherWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<WeatherWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WeatherWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** 日付変更時（00:00）にウィジェットのカレンダー部分を更新するWorkerをスケジュール */
    private fun setupMidnightWidgetWork() {
        // 次の00:00までの遅延を計算
        val now = LocalDateTime.now()
        val nextMidnight = LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT)
        val delay = Duration.between(now, nextMidnight)

        // 24時間周期で実行
        val request = PeriodicWorkRequestBuilder<MidnightWidgetWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MidnightWidgetWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}