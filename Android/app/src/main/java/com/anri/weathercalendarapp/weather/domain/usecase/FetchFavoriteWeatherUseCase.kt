package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import javax.inject.Inject

/** お気に入り地点の緯度経度で天気APIを呼び出し、天気データを取得する */
class FetchFavoriteWeatherUseCase @Inject constructor(
    private val repository: FavoriteLocationRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double): Result<Weather> {
        return repository.fetchWeatherForLocation(lat, lon)
    }
}
