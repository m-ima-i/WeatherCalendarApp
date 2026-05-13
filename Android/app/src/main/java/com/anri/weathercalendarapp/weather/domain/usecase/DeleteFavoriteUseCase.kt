package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import javax.inject.Inject

/** お気に入り地点をRoomから削除する */
class DeleteFavoriteUseCase @Inject constructor(
    private val repository: FavoriteLocationRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return repository.deleteFavorite(id)
    }
}
