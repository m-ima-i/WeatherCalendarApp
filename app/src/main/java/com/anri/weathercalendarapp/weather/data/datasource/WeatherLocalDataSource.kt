package com.anri.weathercalendarapp.weather.data.datasource

import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.weather.domain.model.response.Weather

interface WeatherLocalDataSource {
    // Roomから天気キャッシュを取得する
    suspend fun getCachedWeather(): Weather?

    // Roomから地名キャッシュを取得する
    suspend fun getCachedAddress(): Address?

    // Roomに天気と地名を保存する（地名は同時に上書き。null の場合は既存の地名を保持）
    suspend fun saveWeather(weather: Weather, address: Address?)
}
