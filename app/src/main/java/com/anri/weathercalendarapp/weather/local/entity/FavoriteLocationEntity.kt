package com.anri.weathercalendarapp.weather.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation

@Entity(tableName = "favorite_location_table")
data class FavoriteLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val placeId: String,
    val name: String,
    val secondaryName: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long
)

fun FavoriteLocationEntity.toDomain(): FavoriteLocation {
    return FavoriteLocation(
        id = id,
        placeId = placeId,
        name = name,
        secondaryName = secondaryName,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt
    )
}

fun FavoriteLocation.toEntity(): FavoriteLocationEntity {
    return FavoriteLocationEntity(
        id = id,
        placeId = placeId,
        name = name,
        secondaryName = secondaryName,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt
    )
}
