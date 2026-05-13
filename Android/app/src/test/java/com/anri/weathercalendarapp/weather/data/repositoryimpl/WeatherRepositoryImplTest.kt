package com.anri.weathercalendarapp.weather.data.repositoryimpl

import app.cash.turbine.test
import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.common.geocoder.AppGeocoder
import com.anri.weathercalendarapp.common.location.Location
import com.anri.weathercalendarapp.weather.data.datasource.WeatherLocalDataSource
import com.anri.weathercalendarapp.weather.data.datasource.WeatherRemoteDataSource
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.Current
import com.anri.weathercalendarapp.weather.domain.model.response.Daily
import com.anri.weathercalendarapp.weather.domain.model.response.Hourly
import com.anri.weathercalendarapp.weather.domain.model.response.Temp
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherDescription
import com.anri.weathercalendarapp.widget.data.datasource.WidgetUpdater
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

    private val weatherRemoteDataSource: WeatherRemoteDataSource = mockk()
    private val weatherLocalDataSource: WeatherLocalDataSource = mockk(relaxUnitFun = true)
    private val appGeocoder: AppGeocoder = mockk()
    private val widgetUpdater: WidgetUpdater = mockk(relaxUnitFun = true)

    private lateinit var repository: WeatherRepositoryImpl

    // テスト用データ
    private val testWeatherDescription = WeatherDescription(
        icon = "01d"
    )

    private val testWeather = Weather(
        timezone = "Asia/Tokyo",
        current = Current(
            temp = 20.0,
            feelsLike = 19.5,
            humidity = 60,
            windSpeed = 3.0,
            weather = listOf(testWeatherDescription)
        ),
        hourly = listOf(
            Hourly(
                dt = 1700000000L,
                temp = 20.0,
                pop = 0.1,
                weather = listOf(testWeatherDescription)
            )
        ),
        daily = listOf(
            Daily(
                dt = 1700000000L,
                temp = Temp(min = 15.0, max = 25.0),
                pop = 0.2,
                weather = listOf(testWeatherDescription)
            )
        )
    )

    private val testAddress = Address(
        adminArea = "東京都",
        locality = "渋谷区",
        subLocality = "神南"
    )

    private val testWeatherReq = WeatherReq(lat = 35.6762, lon = 139.6503)
    private val testLocation = Location(latitude = 35.6762, longitude = 139.6503)

    @Before
    fun setup() {
        repository = WeatherRepositoryImpl(
            weatherRemoteDataSource,
            weatherLocalDataSource,
            appGeocoder,
            widgetUpdater
        )
    }

    // ========== getCachedWeather ==========

    @Test
    fun `getCachedWeather - LocalDataSourceに委譲する`() = runTest {
        coEvery { weatherLocalDataSource.getCachedWeather() } returns testWeather

        val result = repository.getCachedWeather()

        assertEquals(testWeather, result)
    }

    @Test
    fun `getCachedWeather - キャッシュなしの場合はnullを返す`() = runTest {
        coEvery { weatherLocalDataSource.getCachedWeather() } returns null

        val result = repository.getCachedWeather()

        assertEquals(null, result)
    }

    // ========== getCachedAddress ==========

    @Test
    fun `getCachedAddress - LocalDataSourceに委譲する`() = runTest {
        coEvery { weatherLocalDataSource.getCachedAddress() } returns testAddress

        val result = repository.getCachedAddress()

        assertEquals(testAddress, result)
    }

    @Test
    fun `getCachedAddress - キャッシュなしの場合はnullを返す`() = runTest {
        coEvery { weatherLocalDataSource.getCachedAddress() } returns null

        val result = repository.getCachedAddress()

        assertEquals(null, result)
    }

    // ========== updateWeather ==========

    @Test
    fun `updateWeather - 正常系 - Loading→Success の順にemitし、天気と地名をRoomに保存する`() = runTest {
        coEvery { weatherRemoteDataSource.getWeather(testWeatherReq) } returns testWeather
        coEvery { appGeocoder.fetchAddress(testLocation) } returns Result.success(testAddress)

        repository.updateWeather(testWeatherReq).test {
            assertTrue(awaitItem() is Resource.Loading)
            val success = awaitItem()
            assertTrue(success is Resource.Success)
            val data = (success as Resource.Success).data!!
            assertEquals(testWeather, data.weather)
            assertEquals(testAddress, data.address)
            awaitComplete()
        }
        coVerify(exactly = 1) { weatherRemoteDataSource.getWeather(testWeatherReq) }
        coVerify(exactly = 1) { appGeocoder.fetchAddress(testLocation) }
        coVerify(exactly = 1) { weatherLocalDataSource.saveWeather(testWeather, testAddress) }
    }

    @Test
    fun `updateWeather - 地名取得失敗時もWeatherはnullの地名で保存される`() = runTest {
        coEvery { weatherRemoteDataSource.getWeather(testWeatherReq) } returns testWeather
        coEvery { appGeocoder.fetchAddress(testLocation) } returns Result.failure(RuntimeException("geocoder error"))

        repository.updateWeather(testWeatherReq).test {
            assertTrue(awaitItem() is Resource.Loading)
            val success = awaitItem()
            assertTrue(success is Resource.Success)
            val data = (success as Resource.Success).data!!
            assertEquals(testWeather, data.weather)
            assertEquals(null, data.address)
            awaitComplete()
        }
        coVerify(exactly = 1) { weatherLocalDataSource.saveWeather(testWeather, null) }
    }

    @Test
    fun `updateWeather - 異常系 - 天気API失敗時はLoading→Errorで、Roomに保存しない`() = runTest {
        val exception = RuntimeException("Network Error")
        coEvery { weatherRemoteDataSource.getWeather(testWeatherReq) } throws exception
        coEvery { appGeocoder.fetchAddress(testLocation) } returns Result.success(testAddress)

        repository.updateWeather(testWeatherReq).test {
            assertTrue(awaitItem() is Resource.Loading)
            val error = awaitItem()
            assertTrue(error is Resource.Error)
            assertEquals("Network Error", (error as Resource.Error).cause?.message)
            awaitComplete()
        }
        coVerify(exactly = 0) { weatherLocalDataSource.saveWeather(any(), any()) }
    }

    // ========== fetchWeather ==========

    @Test
    fun `fetchWeather - 正常系 - 天気と地名を返しRoomに保存する`() = runTest {
        coEvery { weatherRemoteDataSource.getWeather(testWeatherReq) } returns testWeather
        coEvery { appGeocoder.fetchAddress(testLocation) } returns Result.success(testAddress)

        val result = repository.fetchWeather(testWeatherReq)

        assertEquals(testWeather, result.weather)
        assertEquals(testAddress, result.address)
        coVerify(exactly = 1) { weatherRemoteDataSource.getWeather(testWeatherReq) }
        coVerify(exactly = 1) { appGeocoder.fetchAddress(testLocation) }
        coVerify(exactly = 1) { weatherLocalDataSource.saveWeather(testWeather, testAddress) }
    }

    @Test
    fun `fetchWeather - 地名取得失敗時もWeatherはnullの地名で保存される`() = runTest {
        coEvery { weatherRemoteDataSource.getWeather(testWeatherReq) } returns testWeather
        coEvery { appGeocoder.fetchAddress(testLocation) } returns Result.failure(RuntimeException("geocoder error"))

        val result = repository.fetchWeather(testWeatherReq)

        assertEquals(testWeather, result.weather)
        assertEquals(null, result.address)
        coVerify(exactly = 1) { weatherLocalDataSource.saveWeather(testWeather, null) }
    }

    @Test
    fun `fetchWeather - 異常系 - 天気API例外をそのままスローする`() = runTest {
        coEvery { weatherRemoteDataSource.getWeather(testWeatherReq) } throws RuntimeException("Network Error")
        coEvery { appGeocoder.fetchAddress(testLocation) } returns Result.success(testAddress)

        try {
            repository.fetchWeather(testWeatherReq)
            assertTrue("例外がスローされるべき", false)
        } catch (e: RuntimeException) {
            assertEquals("Network Error", e.message)
        }
    }
}
