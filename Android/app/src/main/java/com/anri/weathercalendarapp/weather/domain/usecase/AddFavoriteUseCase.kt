package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.domain.model.response.PlaceSuggestion
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import com.anri.weathercalendarapp.weather.domain.repository.PlacesRepository
import javax.inject.Inject

/** Places SDKで座標を取得し、お気に入り地点としてRoomに保存する */
class AddFavoriteUseCase @Inject constructor(
    private val favoriteLocationRepository: FavoriteLocationRepository,
    private val placesRepository: PlacesRepository
) {
    suspend operator fun invoke(suggestion: PlaceSuggestion): Result<Long> {
        val latLng = placesRepository.getPlaceCoordinates(suggestion.placeId)
            ?: return Result.failure(IllegalStateException("座標を取得できませんでした"))

        val favorite = FavoriteLocation(
            placeId = suggestion.placeId,
            name = suggestion.mainText,
            secondaryName = suggestion.secondaryText,
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )

        return favoriteLocationRepository.addFavorite(favorite)
    }
}
