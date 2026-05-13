package com.anri.weathercalendarapp.weather.remote.apiinterface

import com.anri.weathercalendarapp.weather.remote.model.response.WeatherRemote
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // One Call API 3.0
    @GET("data/3.0/onecall")
    suspend fun getOneCallWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("exclude") exclude: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
    ): WeatherRemote

}