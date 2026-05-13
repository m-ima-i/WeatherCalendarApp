package com.anri.weathercalendarapp.weather.remote.datasourceimpl

import com.anri.weathercalendarapp.common.AppInfo
import com.anri.weathercalendarapp.weather.data.datasource.WeatherRemoteDataSource
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.remote.apiinterface.WeatherApiService
import com.anri.weathercalendarapp.weather.remote.model.request.toRemote
import com.anri.weathercalendarapp.weather.remote.model.response.toDomain
import javax.inject.Inject

class WeatherRemoteDataSourceImpl @Inject constructor(
    private val weatherApiService: WeatherApiService
) : WeatherRemoteDataSource {

    override suspend fun getWeather(req: WeatherReq): Weather {
        val request = req.toRemote()

        val response = weatherApiService.getOneCallWeather(
            lat = request.lat,
            lon = request.lon,
            apiKey = AppInfo.ONE_CALL_API_KEY,
            exclude = request.exclude,
            units = request.units,
            lang = request.lang
        )

        return response.toDomain()
    }

}