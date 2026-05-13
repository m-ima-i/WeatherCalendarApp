package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteFavoriteUseCaseTest {

    private val repository: FavoriteLocationRepository = mockk()
    private val useCase = DeleteFavoriteUseCase(repository)

    @Test
    fun `正常系 - FavoriteLocationRepositoryのdeleteFavoriteに委譲する`() = runTest {
        // Arrange
        coEvery { repository.deleteFavorite(1L) } returns Result.success(Unit)

        // Act
        val result = useCase(1L)

        // Assert
        assertTrue(result.isSuccess)
    }
}
