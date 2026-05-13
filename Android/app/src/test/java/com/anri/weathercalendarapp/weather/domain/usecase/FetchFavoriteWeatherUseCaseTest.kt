package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.model.response.Current
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchFavoriteWeatherUseCaseTest {

    private val repository: FavoriteLocationRepository = mockk()
    private val useCase = FetchFavoriteWeatherUseCase(repository)

    @Test
    fun `正常系 - FavoriteLocationRepositoryのfetchWeatherForLocationに委譲する`() = runTest {
        // Arrange
        val weather = Weather(
            timezone = "Asia/Tokyo",
            current = Current(
                temp = 18.5,
                feelsLike = 17.0,
                humidity = 55,
                windSpeed = 2.5,
                weather = emptyList()
            ),
            hourly = emptyList(),
            daily = emptyList()
        )
        coEvery { repository.fetchWeatherForLocation(35.6586, 139.7454) } returns Result.success(weather)

        // Act
        val result = useCase(35.6586, 139.7454)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(weather, result.getOrNull())
    }
}
