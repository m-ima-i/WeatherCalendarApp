package com.anri.weathercalendarapp.weather.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.anri.weathercalendarapp.common.location.Location
import com.anri.weathercalendarapp.common.location.LocationTracker
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherWithAddress
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherWorkerTest {

    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private val locationTracker: LocationTracker = mockk()
    private val weatherRepository: WeatherRepository = mockk(relaxUnitFun = true)

    private val testLocation = Location(latitude = 35.6762, longitude = 139.6503)
    private val testWeatherReq = WeatherReq(lat = 35.6762, lon = 139.6503)

    private fun createWorker(): WeatherWorker {
        return WeatherWorker(context, workerParams, locationTracker, weatherRepository)
    }

    @Test
    fun `正常系 - 位置情報→天気API→Room保存→success`() = runTest {
        // Arrange
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        coEvery { weatherRepository.fetchWeather(testWeatherReq) } returns mockk<WeatherWithAddress>()

        // Act
        val result = createWorker().doWork()

        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { locationTracker.getCurrentLocation() }
        coVerify(exactly = 1) { weatherRepository.fetchWeather(testWeatherReq) }
    }

    @Test
    fun `異常系 - 位置情報取得失敗→天気API呼ばず即中断`() = runTest {
        // Arrange
        coEvery { locationTracker.getCurrentLocation() } returns null

        // Act
        val result = createWorker().doWork()

        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { locationTracker.getCurrentLocation() }
        coVerify(exactly = 0) { weatherRepository.fetchWeather(any()) }
    }

    @Test
    fun `異常系 - 位置情報例外→天気API呼ばず即中断`() = runTest {
        // Arrange
        coEvery { locationTracker.getCurrentLocation() } throws RuntimeException("GPS Error")

        // Act & Assert
        val result = try {
            createWorker().doWork()
        } catch (_: Exception) {
            ListenableWorker.Result.failure()
        }

        coVerify(exactly = 0) { weatherRepository.fetchWeather(any()) }
    }

    @Test
    fun `異常系 - 天気API失敗→即中断`() = runTest {
        // Arrange
        coEvery { locationTracker.getCurrentLocation() } returns testLocation
        coEvery { weatherRepository.fetchWeather(testWeatherReq) } throws RuntimeException("Network Error")

        // Act
        val result = createWorker().doWork()

        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { locationTracker.getCurrentLocation() }
        coVerify(exactly = 1) { weatherRepository.fetchWeather(testWeatherReq) }
    }
}
