package com.anri.weathercalendarapp.weather.domain.model.response

data class FavoriteLocation(
    val id: Long = 0,
    val placeId: String,
    val name: String,
    val secondaryName: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)
