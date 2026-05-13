package com.anri.weathercalendarapp.weather.remote.datasourceimpl

import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.remote.apiinterface.WeatherApiService
import com.anri.weathercalendarapp.weather.remote.model.response.CurrentRemote
import com.anri.weathercalendarapp.weather.remote.model.response.DailyRemote
import com.anri.weathercalendarapp.weather.remote.model.response.HourlyRemote
import com.anri.weathercalendarapp.weather.remote.model.response.TempRemote
import com.anri.weathercalendarapp.weather.remote.model.response.WeatherDescriptionRemote
import com.anri.weathercalendarapp.weather.remote.model.response.WeatherRemote
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherRemoteDataSourceImplTest {

    private val weatherApiService: WeatherApiService = mockk()
    private val dataSource = WeatherRemoteDataSourceImpl(weatherApiService)

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

    private fun createWeatherRemote(
        lat: Double = 35.6762,
        lon: Double = 139.6503,
        timezone: String = "Asia/Tokyo",
        current: CurrentRemote = createCurrentRemote(),
        hourly: List<HourlyRemote> = listOf(createHourlyRemote()),
        daily: List<DailyRemote> = listOf(createDailyRemote())
    ) = WeatherRemote(
        lat = lat, lon = lon, timezone = timezone,
        current = current, hourly = hourly, daily = daily
    )

    // --- getWeather ---

    @Test
    fun `getWeather正常系 - APIレスポンスをtoDomain変換してWeatherを返す`() = runTest {
        // Arrange
        val req = WeatherReq(lat = 35.6762, lon = 139.6503)
        val remoteResponse = createWeatherRemote()

        coEvery {
            weatherApiService.getOneCallWeather(
                lat = any(),
                lon = any(),
                apiKey = any(),
                exclude = any(),
                units = any(),
                lang = any()
            )
        } returns remoteResponse

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
        assertEquals(1, result.daily[0].weather.size)
        assertEquals(15.0, result.daily[0].temp.min, 0.0001)
        assertEquals(25.0, result.daily[0].temp.max, 0.0001)
        assertEquals(0.2, result.daily[0].pop, 0.0001)
    }

}
