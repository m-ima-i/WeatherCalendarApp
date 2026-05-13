package com.anri.weathercalendarapp.weather.remote.model.response

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherRemoteMapperTest {

    @Test
    fun `WeatherRemote_toDomain - 正常変換`() {
        // Arrange
        val remote = WeatherRemote(
            lat = 35.6762,
            lon = 139.6503,
            timezone = "Asia/Tokyo",
            current = CurrentRemote(
                dt = 1700000000L,
                temp = 20.5,
                feelsLike = 19.0,
                humidity = 60,
                windSpeed = 3.5,
                weather = listOf(
                    WeatherDescriptionRemote(id = 800, icon = "01d")
                )
            ),
            hourly = listOf(
                HourlyRemote(
                    dt = 1700003600L,
                    temp = 21.0,
                    pop = 0.1,
                    weather = listOf(
                        WeatherDescriptionRemote(id = 801, icon = "02d")
                    )
                )
            ),
            daily = listOf(
                DailyRemote(
                    dt = 1700000000L,
                    temp = TempRemote(min = 15.0, max = 25.0),
                    pop = 0.2,
                    weather = listOf(
                        WeatherDescriptionRemote(id = 802, icon = "03d")
                    )
                )
            )
        )

        // Act
        val domain = remote.toDomain()

        // Assert
        assertEquals("Asia/Tokyo", domain.timezone)

        // Current
        assertEquals(20.5, domain.current.temp, 0.0001)
        assertEquals(19.0, domain.current.feelsLike, 0.0001)
        assertEquals(60, domain.current.humidity)
        assertEquals(3.5, domain.current.windSpeed, 0.0001)
        assertEquals(1, domain.current.weather.size)
        assertEquals("01d", domain.current.weather[0].icon)

        // Hourly
        assertEquals(1, domain.hourly.size)
        assertEquals(1700003600L, domain.hourly[0].dt)
        assertEquals(21.0, domain.hourly[0].temp, 0.0001)
        assertEquals(0.1, domain.hourly[0].pop, 0.0001)
        assertEquals(1, domain.hourly[0].weather.size)

        // Daily
        assertEquals(1, domain.daily.size)
        assertEquals(1700000000L, domain.daily[0].dt)
        assertEquals(15.0, domain.daily[0].temp.min, 0.0001)
        assertEquals(25.0, domain.daily[0].temp.max, 0.0001)
        assertEquals(0.2, domain.daily[0].pop, 0.0001)
        assertEquals(1, domain.daily[0].weather.size)
    }

    @Test
    fun `CurrentRemote_toDomain - 正常変換`() {
        // Arrange
        val remote = CurrentRemote(
            dt = 1700000000L,
            temp = 22.3,
            feelsLike = 21.0,
            humidity = 55,
            windSpeed = 4.2,
            weather = listOf(
                WeatherDescriptionRemote(id = 500, icon = "10d")
            )
        )

        // Act
        val domain = remote.toDomain()

        // Assert
        assertEquals(22.3, domain.temp, 0.0001)
        assertEquals(21.0, domain.feelsLike, 0.0001)
        assertEquals(55, domain.humidity)
        assertEquals(4.2, domain.windSpeed, 0.0001)
        assertEquals(1, domain.weather.size)
        assertEquals("10d", domain.weather[0].icon)
    }

    @Test
    fun `HourlyRemote_toDomain - 正常変換`() {
        // Arrange
        val remote = HourlyRemote(
            dt = 1700003600L,
            temp = 18.0,
            pop = 0.75,
            weather = listOf(
                WeatherDescriptionRemote(id = 300, icon = "09d")
            )
        )

        // Act
        val domain = remote.toDomain()

        // Assert
        assertEquals(1700003600L, domain.dt)
        assertEquals(18.0, domain.temp, 0.0001)
        assertEquals(0.75, domain.pop, 0.0001)
        assertEquals(1, domain.weather.size)
        assertEquals("09d", domain.weather[0].icon)
    }

    @Test
    fun `DailyRemote_toDomain - 正常変換`() {
        // Arrange
        val remote = DailyRemote(
            dt = 1700000000L,
            temp = TempRemote(min = 10.0, max = 20.0),
            pop = 0.5,
            weather = listOf(
                WeatherDescriptionRemote(id = 804, icon = "04d")
            )
        )

        // Act
        val domain = remote.toDomain()

        // Assert
        assertEquals(1700000000L, domain.dt)
        assertEquals(10.0, domain.temp.min, 0.0001)
        assertEquals(20.0, domain.temp.max, 0.0001)
        assertEquals(0.5, domain.pop, 0.0001)
        assertEquals(1, domain.weather.size)
    }

    @Test
    fun `WeatherDescriptionRemote_toDomain - 正常変換`() {
        // Arrange
        val remote = WeatherDescriptionRemote(
            id = 800,
            icon = "01n"
        )

        // Act
        val domain = remote.toDomain()

        // Assert
        assertEquals("01n", domain.icon)
    }

    @Test
    fun `TempRemote_toDomain - 正常変換`() {
        // Arrange
        val remote = TempRemote(min = -5.0, max = 10.0)

        // Act
        val domain = remote.toDomain()

        // Assert
        assertEquals(-5.0, domain.min, 0.0001)
        assertEquals(10.0, domain.max, 0.0001)
    }
}
