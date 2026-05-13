package com.anri.weathercalendarapp.weather.remote.model.response

import com.anri.weathercalendarapp.weather.domain.model.response.Current
import com.anri.weathercalendarapp.weather.domain.model.response.Daily
import com.anri.weathercalendarapp.weather.domain.model.response.Hourly
import com.anri.weathercalendarapp.weather.domain.model.response.Temp
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherDescription
import com.google.gson.annotations.SerializedName

data class WeatherRemote(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val current: CurrentRemote,
    val hourly: List<HourlyRemote>,
    val daily: List<DailyRemote>
)

data class CurrentRemote(
    val dt: Long,
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val humidity: Int,
    @SerializedName("wind_speed")
    val windSpeed: Double,
    val weather: List<WeatherDescriptionRemote>
)

data class HourlyRemote(
    val dt: Long,
    val temp: Double,
    val pop: Double,
    val weather: List<WeatherDescriptionRemote>
)

data class DailyRemote(
    val dt: Long,
    val temp: TempRemote,
    val pop: Double,
    val weather: List<WeatherDescriptionRemote>
)

data class WeatherDescriptionRemote(
    val id: Int,
    val icon: String
)

data class TempRemote(
    val min: Double,
    val max: Double
)

fun WeatherRemote.toDomain(): Weather = Weather(
    timezone = timezone,
    current = current.toDomain(),
    hourly = hourly.map { it.toDomain() },
    daily = daily.map { it.toDomain() }
)

fun CurrentRemote.toDomain(): Current = Current(
    temp = temp,
    feelsLike = feelsLike,
    humidity = humidity,
    windSpeed = windSpeed,
    weather = weather.map { it.toDomain() }
)

fun HourlyRemote.toDomain(): Hourly = Hourly(
    dt = dt,
    temp = temp,
    pop = pop,
    weather = weather.map { it.toDomain() }
)

fun DailyRemote.toDomain(): Daily = Daily(
    dt = dt,
    temp = temp.toDomain(),
    pop = pop,
    weather = weather.map { it.toDomain() }
)

fun WeatherDescriptionRemote.toDomain(): WeatherDescription = WeatherDescription(
    icon = icon
)

fun TempRemote.toDomain(): Temp = Temp(
    min = min,
    max = max
)