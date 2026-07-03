package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** お気に入り地点の一覧をFlowで監視する */
class GetFavoritesStreamUseCase @Inject constructor(
    private val repository: FavoriteLocationRepository
) {
    operator fun invoke(): Flow<List<FavoriteLocation>> {
        return repository.getFavoritesStream()
    }
}
