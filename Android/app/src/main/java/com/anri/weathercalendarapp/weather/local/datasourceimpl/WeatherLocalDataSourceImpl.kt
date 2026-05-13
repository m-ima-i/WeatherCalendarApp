package com.anri.weathercalendarapp.weather.local.datasourceimpl

import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.weather.data.datasource.WeatherLocalDataSource
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.local.dao.WeatherDAO
import com.anri.weathercalendarapp.weather.local.entity.WeatherEntity
import com.google.gson.Gson
import javax.inject.Inject

class WeatherLocalDataSourceImpl @Inject constructor(
    private val weatherDAO: WeatherDAO
) : WeatherLocalDataSource {

    private val gson = Gson()

    override suspend fun getCachedWeather(): Weather? {
        return weatherDAO.getWeather()?.let {
            gson.fromJson(it.weatherJson, Weather::class.java)
        }
    }

    override suspend fun getCachedAddress(): Address? {
        val entity = weatherDAO.getWeather() ?: return null
        val json = entity.addressJson ?: return null
        return gson.fromJson(json, Address::class.java)
    }

    override suspend fun saveWeather(weather: Weather, address: Address?) {
        // address=null（Geocoder失敗）の場合は既存の addressJson を保持する。
        // TopBar 側も `currentAddress = result.data.address ?: it.currentAddress` で
        // 古い地名を維持するため、Local も同じ挙動に揃える。
        val newAddressJson = if (address != null) {
            gson.toJson(address)
        } else {
            weatherDAO.getWeather()?.addressJson
        }
        val entity = WeatherEntity(
            weatherJson = gson.toJson(weather),
            addressJson = newAddressJson
        )
        weatherDAO.insertWeather(entity)
    }
}
