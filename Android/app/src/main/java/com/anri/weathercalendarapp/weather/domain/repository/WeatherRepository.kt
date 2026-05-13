package com.anri.weathercalendarapp.weather.domain.repository

import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherWithAddress
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    // Roomから天気キャッシュを取得する
    suspend fun getCachedWeather(): Weather?

    // Roomから地名キャッシュを取得する
    suspend fun getCachedAddress(): Address?

    // API取得→Room保存→Weather+Addressを返す（ViewModel向け: Loading/Success/Error ストリーム）
    fun updateWeather(req: WeatherReq): Flow<Resource<WeatherWithAddress>>

    // API取得→Room保存（Worker用: 単発呼び出し）
    suspend fun fetchWeather(req: WeatherReq): WeatherWithAddress
}
