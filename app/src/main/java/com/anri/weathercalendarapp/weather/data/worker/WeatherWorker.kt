package com.anri.weathercalendarapp.weather.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anri.weathercalendarapp.common.location.LocationTracker
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** 定期的に天気をAPI取得しRoomに保存するWorker（ウィジェット再描画はRepository層で実行） */
@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationTracker: LocationTracker,
    private val weatherRepository: WeatherRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val location = locationTracker.getCurrentLocation()
        if (location == null) {
            return Result.success()
        }

        try {
            weatherRepository.fetchWeather(
                WeatherReq(lat = location.latitude, lon = location.longitude)
            )
        } catch (e: Exception) {
            return Result.success()
        }

        return Result.success()
    }
}
