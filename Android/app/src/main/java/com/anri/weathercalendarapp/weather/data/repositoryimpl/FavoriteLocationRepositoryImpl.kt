package com.anri.weathercalendarapp.weather.data.repositoryimpl

import com.anri.weathercalendarapp.weather.data.datasource.FavoriteLocationLocalDataSource
import com.anri.weathercalendarapp.weather.data.datasource.WeatherRemoteDataSource
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocation
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoriteLocationRepositoryImpl @Inject constructor(
    private val favoriteLocalDataSource: FavoriteLocationLocalDataSource,
    private val weatherRemoteDataSource: WeatherRemoteDataSource
) : FavoriteLocationRepository {

    override fun getFavoritesStream(): Flow<List<FavoriteLocation>> {
        return favoriteLocalDataSource.getFavoritesStream()
    }

    override suspend fun addFavorite(favorite: FavoriteLocation): Result<Long> {
        return try {
            if (favoriteLocalDataSource.existsByPlaceId(favorite.placeId)) {
                Result.failure(IllegalStateException("登録済みの地域です"))
            } else {
                val id = favoriteLocalDataSource.addFavorite(favorite)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFavorite(id: Long): Result<Unit> {
        return try {
            favoriteLocalDataSource.deleteFavorite(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchWeatherForLocation(lat: Double, lon: Double): Result<Weather> {
        return try {
            val weather = weatherRemoteDataSource.getWeather(WeatherReq(lat = lat, lon = lon))
            Result.success(weather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFavoriteByIdStream(id: Long): Flow<FavoriteLocation?> {
        return favoriteLocalDataSource.getFavoriteByIdStream(id)
    }
}
