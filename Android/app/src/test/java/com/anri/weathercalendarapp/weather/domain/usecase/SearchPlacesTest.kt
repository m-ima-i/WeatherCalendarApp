package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.anri.weathercalendarapp.weather.domain.repository.PlacesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchPlacesTest {

    private val placesRepository: PlacesRepository = mockk()
    private val useCase = SearchPlaces(placesRepository)

    @Test
    fun `正常系 - PlacesRepositoryのsearchPlacesに委譲する`() = runTest {
        // Arrange
        val suggestions = listOf(
            PlaceSuggestion(
                placeId = "place_1",
                mainText = "東京駅",
                secondaryText = "東京都千代田区"
            )
        )
        coEvery { placesRepository.searchPlaces("東京") } returns suggestions

        // Act
        val result = useCase("東京")

        // Assert
        assertEquals(suggestions, result)
    }
}
