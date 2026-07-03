package com.anri.weathercalendarapp.weather.data.datasource

import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.Weather

interface WeatherRemoteDataSource {

    /** OpenWeatherMap One Call APIを呼び出してWeatherを取得する */
    suspend fun getWeather(req: WeatherReq): Weather
}