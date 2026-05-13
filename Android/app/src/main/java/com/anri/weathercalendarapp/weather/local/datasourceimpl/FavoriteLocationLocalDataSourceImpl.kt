package com.anri.weathercalendarapp.weather.local.datasourceimpl

import com.anri.weathercalendarapp.weather.data.datasource.FavoriteLocationLocalDataSource
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.local.dao.FavoriteLocationDAO
import com.anri.weathercalendarapp.weather.local.entity.toDomain
import com.anri.weathercalendarapp.weather.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteLocationLocalDataSourceImpl @Inject constructor(
    private val favoriteLocationDAO: FavoriteLocationDAO
) : FavoriteLocationLocalDataSource {

    override fun getFavoritesStream(): Flow<List<FavoriteLocation>> {
        return favoriteLocationDAO.getAllFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addFavorite(favorite: FavoriteLocation): Long {
        return favoriteLocationDAO.insertFavorite(favorite.toEntity())
    }

    override suspend fun deleteFavorite(id: Long) {
        favoriteLocationDAO.deleteFavoriteById(id)
    }

    override suspend fun existsByPlaceId(placeId: String): Boolean {
        return favoriteLocationDAO.getFavoriteByPlaceId(placeId) != null
    }

    override fun getFavoriteByIdStream(id: Long): Flow<FavoriteLocation?> {
        return favoriteLocationDAO.getFavoriteById(id).map { it?.toDomain() }
    }
}
