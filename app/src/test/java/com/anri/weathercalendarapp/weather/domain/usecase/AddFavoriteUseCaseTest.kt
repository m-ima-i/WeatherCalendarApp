package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import com.anri.weathercalendarapp.weather.domain.repository.PlacesRepository
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddFavoriteUseCaseTest {

    private val favoriteLocationRepository: FavoriteLocationRepository = mockk()
    private val placesRepository: PlacesRepository = mockk()
    private lateinit var useCase: AddFavoriteUseCase

    @Before
    fun setup() {
        useCase = AddFavoriteUseCase(favoriteLocationRepository, placesRepository)
    }

    @Test
    fun `正常系 - 座標取得成功かつ登録成功`() = runTest {
        // Arrange
        val suggestion = PlaceSuggestion(
            placeId = "place_123",
            mainText = "東京タワー",
            secondaryText = "東京都港区"
        )
        val latLng = LatLng(35.6586, 139.7454)
        coEvery { placesRepository.getPlaceCoordinates("place_123") } returns latLng
        coEvery { favoriteLocationRepository.addFavorite(any()) } returns Result.success(1L)

        // Act
        val result = useCase(suggestion)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify {
            favoriteLocationRepository.addFavorite(
                match {
                    it.placeId == "place_123" &&
                    it.name == "東京タワー" &&
                    it.secondaryName == "東京都港区" &&
                    it.latitude == 35.6586 &&
                    it.longitude == 139.7454
                }
            )
        }
    }

    @Test
    fun `異常系 - 座標がnullの場合は失敗を返す`() = runTest {
        // Arrange
        val suggestion = PlaceSuggestion(
            placeId = "place_invalid",
            mainText = "不明な場所",
            secondaryText = "不明"
        )
        coEvery { placesRepository.getPlaceCoordinates("place_invalid") } returns null

        // Act
        val result = useCase(suggestion)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals("座標を取得できませんでした", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { favoriteLocationRepository.addFavorite(any()) }
    }

    @Test
    fun `異常系 - addFavoriteが失敗した場合はそのまま伝播する`() = runTest {
        // Arrange
        val suggestion = PlaceSuggestion(
            placeId = "place_456",
            mainText = "大阪城",
            secondaryText = "大阪府大阪市"
        )
        val latLng = LatLng(34.6873, 135.5259)
        val exception = RuntimeException("DB書き込みエラー")
        coEvery { placesRepository.getPlaceCoordinates("place_456") } returns latLng
        coEvery { favoriteLocationRepository.addFavorite(any()) } returns Result.failure(exception)

        // Act
        val result = useCase(suggestion)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("DB書き込みエラー", result.exceptionOrNull()?.message)
    }
}
