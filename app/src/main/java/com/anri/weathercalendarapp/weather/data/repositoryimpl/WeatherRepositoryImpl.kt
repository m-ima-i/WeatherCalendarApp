package com.anri.weathercalendarapp.weather.data.repositoryimpl

import com.anri.weathercalendarapp.common.Resource
import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.common.geocoder.AppGeocoder
import com.anri.weathercalendarapp.common.location.Location
import com.anri.weathercalendarapp.weather.data.datasource.WeatherLocalDataSource
import com.anri.weathercalendarapp.weather.data.datasource.WeatherRemoteDataSource
import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherWithAddress
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import com.anri.weathercalendarapp.widget.data.datasource.WidgetUpdater
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val weatherLocalDataSource: WeatherLocalDataSource,
    private val appGeocoder: AppGeocoder,
    private val widgetUpdater: WidgetUpdater,
) : WeatherRepository {

    override suspend fun getCachedWeather(): Weather? {
        return weatherLocalDataSource.getCachedWeather()
    }

    override suspend fun getCachedAddress(): Address? {
        return weatherLocalDataSource.getCachedAddress()
    }

    override suspend fun fetchWeather(req: WeatherReq): WeatherWithAddress {
        val (weather, address) = fetchWeatherAndAddress(req)
        weatherLocalDataSource.saveWeather(weather, address)
        widgetUpdater.refreshWeatherWidgets()
        return WeatherWithAddress(weather, address)
    }

    override fun updateWeather(req: WeatherReq): Flow<Resource<WeatherWithAddress>> = flow {
        emit(Resource.Loading())

        try {
            val (weather, address) = fetchWeatherAndAddress(req)
            weatherLocalDataSource.saveWeather(weather, address)
            widgetUpdater.refreshWeatherWidgets()
            emit(Resource.Success(WeatherWithAddress(weather, address)))
        } catch (e: Exception) {
            emit(Resource.Error(cause = e))
        }
    }

    /** 天気APIと地名取得を並行実行する。地名は失敗してもnullで継続。 */
    private suspend fun fetchWeatherAndAddress(req: WeatherReq): Pair<Weather, Address?> =
        coroutineScope {
            val weatherDeferred = async { weatherRemoteDataSource.getWeather(req) }
            val addressDeferred = async {
                appGeocoder.fetchAddress(Location(req.lat, req.lon)).getOrNull()
            }
            weatherDeferred.await() to addressDeferred.await()
        }
}
