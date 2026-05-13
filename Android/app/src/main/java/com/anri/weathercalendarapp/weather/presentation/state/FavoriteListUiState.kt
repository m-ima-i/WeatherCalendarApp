package com.anri.weathercalendarapp.weather.presentation.state

import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocationWeatherSummary
import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.anri.weathercalendarapp.weather.presentation.type.PlaceFailureType

data class FavoriteListUiState(
    val favorites: List<FavoriteLocationWeatherSummary> = emptyList(),
    val searchSuggestions: List<PlaceSuggestion> = emptyList(),
    val searchFailureType: PlaceFailureType? = null,
    val isAddingFavorite: Boolean = false,
)
