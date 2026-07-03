package com.anri.weathercalendarapp.weather.domain.model.response

data class Weather(
    val timezone: String,
    val current: Current,
    val hourly: List<Hourly>,
    val daily: List<Daily>
)

data class Current(
    val temp: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weather: List<WeatherDescription>
)

data class Hourly(
    val dt: Long,
    val temp: Double,
    val pop: Double,
    val weather: List<WeatherDescription>
)

data class Daily(
    val dt: Long,
    val temp: Temp,
    val pop: Double,
    val weather: List<WeatherDescription>
)

data class WeatherDescription(
    val icon: String
)

data class Temp(
    val min: Double,
    val max: Double
)
