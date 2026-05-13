package com.anri.weathercalendarapp.weather.local.entity

import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteLocationEntityMapperTest {

    @Test
    fun `FavoriteLocation_toEntity - DomainからEntityへの変換`() {
        // Arrange
        val domain = FavoriteLocation(
            id = 5L,
            placeId = "place_abc",
            name = "東京タワー",
            secondaryName = "東京都港区芝公園",
            latitude = 35.6586,
            longitude = 139.7454,
            createdAt = 1700000000000L
        )

        // Act
        val entity = domain.toEntity()

        // Assert
        assertEquals(5L, entity.id)
        assertEquals("place_abc", entity.placeId)
        assertEquals("東京タワー", entity.name)
        assertEquals("東京都港区芝公園", entity.secondaryName)
        assertEquals(35.6586, entity.latitude, 0.0001)
        assertEquals(139.7454, entity.longitude, 0.0001)
        assertEquals(1700000000000L, entity.createdAt)
    }

    @Test
    fun `FavoriteLocationEntity_toDomain - EntityからDomainへの変換`() {
        // Arrange
        val entity = FavoriteLocationEntity(
            id = 3L,
            placeId = "place_xyz",
            name = "大阪城",
            secondaryName = "大阪府大阪市中央区",
            latitude = 34.6873,
            longitude = 135.5259,
            createdAt = 1700000000000L
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertEquals(3L, domain.id)
        assertEquals("place_xyz", domain.placeId)
        assertEquals("大阪城", domain.name)
        assertEquals("大阪府大阪市中央区", domain.secondaryName)
        assertEquals(34.6873, domain.latitude, 0.0001)
        assertEquals(135.5259, domain.longitude, 0.0001)
        assertEquals(1700000000000L, domain.createdAt)
    }

    @Test
    fun `往復変換 - Domain → Entity → Domain で同一性が保たれる`() {
        // Arrange
        val original = FavoriteLocation(
            id = 10L,
            placeId = "place_round",
            name = "富士山",
            secondaryName = "静岡県",
            latitude = 35.3606,
            longitude = 138.7274,
            createdAt = 1700000000000L
        )

        // Act
        val roundTripped = original.toEntity().toDomain()

        // Assert
        assertEquals(original, roundTripped)
    }
}
