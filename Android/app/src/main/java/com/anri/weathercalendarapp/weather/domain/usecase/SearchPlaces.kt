package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.anri.weathercalendarapp.weather.domain.repository.PlacesRepository
import javax.inject.Inject

/** Places SDKで地名を検索し、候補一覧を取得する */
class SearchPlaces @Inject constructor(
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(query: String): List<PlaceSuggestion> {
        return placesRepository.searchPlaces(query)
    }
}