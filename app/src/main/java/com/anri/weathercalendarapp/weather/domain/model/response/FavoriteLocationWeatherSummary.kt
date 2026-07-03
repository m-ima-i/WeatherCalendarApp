package com.anri.weathercalendarapp.weather.domain.model.response

import com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType

data class FavoriteLocationWeatherSummary(
    val favoriteLocation: FavoriteLocation,
    val currentTemp: Double? = null,
    val maxTemp: Double? = null,
    val minTemp: Double? = null,
    val weatherIcon: String? = null,
    val timezone: String? = null,
    val isLoading: Boolean = false,
    val isRetrying: Boolean = false,
    val failureType: WeatherFailureType? = null
)
