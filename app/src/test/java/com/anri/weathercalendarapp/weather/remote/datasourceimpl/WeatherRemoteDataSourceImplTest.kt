package com.anri.weathercalendarapp.weather.remote.datasourceimpl

import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.remote.apiinterface.WeatherApiService
import com.anri.weathercalendarapp.weather.remote.model.response.CurrentRemote
import com.anri.weathercalendarapp.weather.remote.model.response.CurrentResponseRemote
import com.anri.weathercalendarapp.weather.remote.model.response.DailyRemote
import com.anri.weathercalendarapp.weather.remote.model.response.DailyResponseRemote
import com.anri.weathercalendarapp.weather.remote.model.response.HourlyRemote
import com.anri.weathercalendarapp.weather.remote.model.response.HourlyResponseRemote
import com.anri.weathercalendarapp.weather.remote.model.response.TempRemote
import com.anri.weathercalendarapp.weather.remote.model.response.WeatherDescriptionRemote
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRemoteDataSourceImplTest {

    private val weatherApiService: WeatherApiService = mockk()
    private val dataSource = WeatherRemoteDataSourceImpl(weatherApiService)

    private val req = WeatherReq(lat = 35.6762, lon = 139.6503)

    // --- テスト用ヘルパー ---

    private fun createWeatherDescriptionRemote(
        id: Int = 800,
        icon: String = "01d"
    ) = WeatherDescriptionRemote(id = id, icon = icon)

    private fun createCurrentRemote(
        dt: Long = 1700000000L,
        temp: Double = 20.5,
        feelsLike: Double = 19.0,
        humidity: Int = 60,
        windSpeed: Double = 3.5,
        weather: List<WeatherDescriptionRemote> = listOf(createWeatherDescriptionRemote())
    ) = CurrentRemote(
        dt = dt, temp = temp, feelsLike = feelsLike,
        humidity = humidity, windSpeed = windSpeed, weather = weather
    )

    private fun createHourlyRemote(
        dt: Long = 1700003600L,
        temp: Double = 21.0,
        pop: Double = 0.1,
        weather: List<WeatherDescriptionRemote> = listOf(createWeatherDescriptionRemote())
    ) = HourlyRemote(dt = dt, temp = temp, pop = pop, weather = weather)

    private fun createDailyRemote(
        dt: Long = 1700000000L,
        temp: TempRemote = TempRemote(min = 15.0, max = 25.0),
        pop: Double = 0.2,
        weather: List<WeatherDescriptionRemote> = listOf(createWeatherDescriptionRemote())
    ) = DailyRemote(dt = dt, temp = temp, pop = pop, weather = weather)

    /** startを起点に1時間刻みでcount件の時間別データを作る */
    private fun createHourlyPage(start: Long, count: Int): List<HourlyRemote> =
        (0 until count).map { createHourlyRemote(dt = start + it * HOUR) }

    private fun stubCurrent(
        response: CurrentResponseRemote = CurrentResponseRemote(
            timezone = TIMEZONE,
            data = listOf(createCurrentRemote())
        )
    ) {
        coEvery {
            weatherApiService.getCurrentWeather(
                lat = any(), lon = any(), apiKey = any(), units = any(), lang = any()
            )
        } returns response
    }

    private fun stubDaily(
        response: DailyResponseRemote = DailyResponseRemote(
            timezone = TIMEZONE,
            data = listOf(createDailyRemote())
        )
    ) {
        coEvery {
            weatherApiService.getDailyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = any()
            )
        } returns response
    }

    // --- getWeather ---

    @Test
    fun `getWeather正常系 - 3エンドポイントの結果をWeatherにまとめる`() = runTest {
        // Arrange
        stubCurrent()
        stubDaily()
        coEvery {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = null
            )
        } returns HourlyResponseRemote(
            timezone = TIMEZONE,
            data = listOf(createHourlyRemote()),
            next = null
        )

        // Act
        val result = dataSource.getWeather(req)

        // Assert
        assertEquals("Asia/Tokyo", result.timezone)
        assertEquals(20.5, result.current.temp, 0.0001)
        assertEquals(19.0, result.current.feelsLike, 0.0001)
        assertEquals(60, result.current.humidity)
        assertEquals(3.5, result.current.windSpeed, 0.0001)
        assertEquals(1, result.current.weather.size)
        assertEquals("01d", result.current.weather[0].icon)
        assertEquals(1, result.hourly.size)
        assertEquals(21.0, result.hourly[0].temp, 0.0001)
        assertEquals(0.1, result.hourly[0].pop, 0.0001)
        assertEquals(1, result.daily.size)
        assertEquals(15.0, result.daily[0].temp.min, 0.0001)
        assertEquals(25.0, result.daily[0].temp.max, 0.0001)
        assertEquals(0.2, result.daily[0].pop, 0.0001)
    }

    @Test
    fun `getWeather - 時間別が20件かつnextありの場合は2ページ目を結合する`() = runTest {
        // Arrange
        stubCurrent()
        stubDaily()
        val firstPageStart = 1700003600L
        val secondPageStart = firstPageStart + 20 * HOUR

        coEvery {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = null
            )
        } returns HourlyResponseRemote(
            timezone = TIMEZONE,
            data = createHourlyPage(start = firstPageStart, count = 20),
            next = NEXT_URL
        )
        coEvery {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = secondPageStart
            )
        } returns HourlyResponseRemote(
            timezone = TIMEZONE,
            data = createHourlyPage(start = secondPageStart, count = 20),
            next = null
        )

        // Act
        val result = dataSource.getWeather(req)

        // Assert
        assertEquals(40, result.hourly.size)
        assertEquals(firstPageStart, result.hourly[0].dt)
        // 1ページ目の最後の次の時刻から2ページ目が連続していること
        assertEquals(secondPageStart, result.hourly[20].dt)
        coVerify(exactly = 1) {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = secondPageStart
            )
        }
    }

    @Test
    fun `getWeather - 時間別が24件以上あれば2ページ目を取得しない`() = runTest {
        // Arrange
        stubCurrent()
        stubDaily()
        coEvery {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = null
            )
        } returns HourlyResponseRemote(
            timezone = TIMEZONE,
            data = createHourlyPage(start = 1700003600L, count = 24),
            next = NEXT_URL
        )

        // Act
        val result = dataSource.getWeather(req)

        // Assert
        assertEquals(24, result.hourly.size)
        coVerify(exactly = 1) {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = any()
            )
        }
    }

    @Test
    fun `getWeather - 時間別のnextがnullなら2ページ目を取得しない`() = runTest {
        // Arrange
        stubCurrent()
        stubDaily()
        coEvery {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = null
            )
        } returns HourlyResponseRemote(
            timezone = TIMEZONE,
            data = createHourlyPage(start = 1700003600L, count = 20),
            next = null
        )

        // Act
        val result = dataSource.getWeather(req)

        // Assert
        assertEquals(20, result.hourly.size)
        coVerify(exactly = 1) {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = any()
            )
        }
    }

    @Test
    fun `getWeather異常系 - currentのdataが空なら例外を投げる`() = runTest {
        // Arrange
        stubCurrent(CurrentResponseRemote(timezone = TIMEZONE, data = emptyList()))
        stubDaily()
        coEvery {
            weatherApiService.getHourlyTimeline(
                lat = any(), lon = any(), apiKey = any(),
                units = any(), lang = any(), start = null
            )
        } returns HourlyResponseRemote(
            timezone = TIMEZONE,
            data = listOf(createHourlyRemote()),
            next = null
        )

        // Act
        val exception = runCatching { dataSource.getWeather(req) }.exceptionOrNull()

        // Assert
        assertTrue(exception is IllegalStateException)
    }

    private companion object {
        const val TIMEZONE = "Asia/Tokyo"
        const val HOUR = 3600L
        const val NEXT_URL =
            "http://api.openweathermap.org/data/4.0/onecall/timeline/1h?cnt=20&start=1789851600"
    }
}
