package com.anri.weathercalendarapp.weather.domain.usecase

import app.cash.turbine.test
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFavoritesStreamUseCaseTest {

    private val repository: FavoriteLocationRepository = mockk()
    private val useCase = GetFavoritesStreamUseCase(repository)

    @Test
    fun `正常系 - FavoriteLocationRepositoryのgetFavoritesStreamに委譲する`() = runTest {
        // Arrange
        val favorites = listOf(
            FavoriteLocation(
                id = 1L,
                placeId = "place_1",
                name = "東京タワー",
                secondaryName = "東京都港区",
                latitude = 35.6586,
                longitude = 139.7454,
                createdAt = 1700000000000L
            )
        )
        every { repository.getFavoritesStream() } returns flowOf(favorites)

        // Act & Assert
        useCase().test {
            assertEquals(favorites, awaitItem())
            awaitComplete()
        }
    }
}
