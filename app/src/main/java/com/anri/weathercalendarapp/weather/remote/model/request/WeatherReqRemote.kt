package com.anri.weathercalendarapp.weather.remote.model.request

import com.anri.weathercalendarapp.weather.domain.model.request.WeatherReq

data class WeatherReqRemote(
    val lat: Double,
    val lon: Double,
    val exclude: String = "minutely,alerts", // デフォルトで不要な情報を除外
    val units: String = "metric", // デフォルトで摂氏に指定
    val lang: String = "ja" // デフォルトで日本語に指定
)

fun WeatherReq.toRemote(): WeatherReqRemote = WeatherReqRemote(
    lat = lat,
    lon = lon
)