package com.anri.weathercalendarapp.weather.local.datasourceimpl

import app.cash.turbine.test
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.local.dao.FavoriteLocationDAO
import com.anri.weathercalendarapp.weather.local.entity.FavoriteLocationEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteLocationLocalDataSourceImplTest {

    private val favoriteLocationDAO: FavoriteLocationDAO = mockk(relaxed = true)
    private val dataSource = FavoriteLocationLocalDataSourceImpl(favoriteLocationDAO)

    // --- テスト用ヘルパー ---

    private fun createFavoriteLocationEntity(
        id: Long = 1L,
        placeId: String = "place_001",
        name: String = "東京タワー",
        secondaryName: String = "東京都港区",
        latitude: Double = 35.6586,
        longitude: Double = 139.7454,
        createdAt: Long = 1700000000L
    ) = FavoriteLocationEntity(
        id = id, placeId = placeId, name = name,
        secondaryName = secondaryName, latitude = latitude,
        longitude = longitude, createdAt = createdAt
    )

    private fun createFavoriteLocation(
        id: Long = 1L,
        placeId: String = "place_001",
        name: String = "東京タワー",
        secondaryName: String = "東京都港区",
        latitude: Double = 35.6586,
        longitude: Double = 139.7454,
        createdAt: Long = 1700000000L
    ) = FavoriteLocation(
        id = id, placeId = placeId, name = name,
        secondaryName = secondaryName, latitude = latitude,
        longitude = longitude, createdAt = createdAt
    )

    // --- getFavoritesStream ---

    @Test
    fun `getFavoritesStream正常系 - DAOからtoDomain変換リストを返す`() = runTest {
        // Arrange
        val entities = listOf(
            createFavoriteLocationEntity(id = 1L, placeId = "place_001", name = "東京タワー"),
            createFavoriteLocationEntity(id = 2L, placeId = "place_002", name = "スカイツリー")
        )
        every { favoriteLocationDAO.getAllFavorites() } returns flowOf(entities)

        // Act & Assert
        dataSource.getFavoritesStream().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(1L, result[0].id)
            assertEquals("place_001", result[0].placeId)
            assertEquals("東京タワー", result[0].name)
            assertEquals(2L, result[1].id)
            assertEquals("place_002", result[1].placeId)
            assertEquals("スカイツリー", result[1].name)
            awaitComplete()
        }
    }

    @Test
    fun `getFavoritesStream境界値 - 空リストの場合は空リストを返す`() = runTest {
        // Arrange
        every { favoriteLocationDAO.getAllFavorites() } returns flowOf(emptyList())

        // Act & Assert
        dataSource.getFavoritesStream().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    // --- addFavorite ---

    @Test
    fun `addFavorite正常系 - toEntity変換してDAOに保存しIDを返す`() = runTest {
        // Arrange
        val favorite = createFavoriteLocation()
        coEvery { favoriteLocationDAO.insertFavorite(any()) } returns 1L

        // Act
        val result = dataSource.addFavorite(favorite)

        // Assert
        assertEquals(1L, result)
        coVerify {
            favoriteLocationDAO.insertFavorite(withArg { entity ->
                assertEquals("place_001", entity.placeId)
                assertEquals("東京タワー", entity.name)
                assertEquals("東京都港区", entity.secondaryName)
                assertEquals(35.6586, entity.latitude, 0.0001)
                assertEquals(139.7454, entity.longitude, 0.0001)
            })
        }
    }

    // --- deleteFavorite ---

    @Test
    fun `deleteFavorite正常系 - DAOに委譲する`() = runTest {
        // Arrange
        val id = 1L
        coEvery { favoriteLocationDAO.deleteFavoriteById(id) } returns Unit

        // Act
        dataSource.deleteFavorite(id)

        // Assert
        coVerify { favoriteLocationDAO.deleteFavoriteById(id) }
    }

    // --- existsByPlaceId ---

    @Test
    fun `existsByPlaceId - 存在する場合はtrueを返す`() = runTest {
        // Arrange
        val placeId = "place_001"
        coEvery { favoriteLocationDAO.getFavoriteByPlaceId(placeId) } returns createFavoriteLocationEntity()

        // Act
        val result = dataSource.existsByPlaceId(placeId)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `existsByPlaceId - 存在しない場合はfalseを返す`() = runTest {
        // Arrange
        val placeId = "place_999"
        coEvery { favoriteLocationDAO.getFavoriteByPlaceId(placeId) } returns null

        // Act
        val result = dataSource.existsByPlaceId(placeId)

        // Assert
        assertFalse(result)
    }
}
