package com.anri.weathercalendarapp.weather.domain.usecase

import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import javax.inject.Inject

/** Roomから天気キャッシュを取得する */
class GetCachedWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(): Weather? = weatherRepository.getCachedWeather()
}
