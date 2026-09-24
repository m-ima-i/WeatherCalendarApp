package com.anri.weathercalendarapp.weather.remote.apiinterface

import com.anri.weathercalendarapp.weather.remote.model.response.CurrentResponseRemote
import com.anri.weathercalendarapp.weather.remote.model.response.DailyResponseRemote
import com.anri.weathercalendarapp.weather.remote.model.response.HourlyResponseRemote
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // One Call API 4.0 - 現在の天気（dataは1件の配列）
    @GET("data/4.0/onecall/current")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
    ): CurrentResponseRemote

    // One Call API 4.0 - 時間別タイムライン（1レスポンス最大20件）
    @GET("data/4.0/onecall/timeline/1h")
    suspend fun getHourlyTimeline(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("start") start: Long? = null,
    ): HourlyResponseRemote

    // One Call API 4.0 - 日別タイムライン（1レスポンス最大10件）
    @GET("data/4.0/onecall/timeline/1day")
    suspend fun getDailyTimeline(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("start") start: Long? = null,
    ): DailyResponseRemote

}
