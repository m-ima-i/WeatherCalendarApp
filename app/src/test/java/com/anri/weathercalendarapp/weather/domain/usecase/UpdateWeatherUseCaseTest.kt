package com.anri.weathercalendarapp.weather.domain.usecase

import app.cash.turbine.test
import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.location.Location
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherWithAddress
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateWeatherUseCaseTest {

    private val weatherRepository: WeatherRepository = mockk(relaxUnitFun = true)
    private lateinit var useCase: UpdateWeatherUseCase

    @Before
    fun setup() {
        useCase = UpdateWeatherUseCase(weatherRepository)
    }

    @Test
    fun `正常系 - Locationの座標でWeatherReqを構築しFlowをそのまま返す`() = runTest {
        // Arrange
        val location = Location(latitude = 35.6762, longitude = 139.6503)
        every {
            weatherRepository.updateWeather(WeatherReq(lat = 35.6762, lon = 139.6503))
        } returns flowOf(Resource.Loading(), Resource.Success(mockk<WeatherWithAddress>()))

        // Act & Assert
        useCase(location).test {
            assertTrue(awaitItem() is Resource.Loading)
            assertTrue(awaitItem() is Resource.Success)
            awaitComplete()
        }
    }

    @Test
    fun `異常系 - エラーFlowもそのまま透過する`() = runTest {
        // Arrange
        val location = Location(latitude = 35.6762, longitude = 139.6503)
        every {
            weatherRepository.updateWeather(WeatherReq(lat = 35.6762, lon = 139.6503))
        } returns flowOf(Resource.Loading(), Resource.Error(cause = RuntimeException("ネットワークエラー")))

        // Act & Assert
        useCase(location).test {
            assertTrue(awaitItem() is Resource.Loading)
            assertTrue(awaitItem() is Resource.Error)
            awaitComplete()
        }
    }
}
