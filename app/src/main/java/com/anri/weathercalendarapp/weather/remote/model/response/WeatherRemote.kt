package com.anri.weathercalendarapp.weather.remote.model.response

import com.anri.weathercalendarapp.weather.domain.model.response.Current
import com.anri.weathercalendarapp.weather.domain.model.response.Daily
import com.anri.weathercalendarapp.weather.domain.model.response.Hourly
import com.anri.weathercalendarapp.weather.domain.model.response.Temp
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.domain.model.response.WeatherDescription
import com.google.gson.annotations.SerializedName

/**
 * One Call API 4.0 の GET data/4.0/onecall/current のレスポンス。
 * 3.0 の current オブジェクトは 4.0 では1件だけのdata配列になった。
 */
data class CurrentResponseRemote(
    val timezone: String,
    val data: List<CurrentRemote>
)

/**
 * One Call API 4.0 の GET data/4.0/onecall/timeline/1h のレスポンス。
 * 1レスポンス最大20件で、続きはnext（前はprev）の有無で判断する。
 */
data class HourlyResponseRemote(
    val timezone: String,
    val data: List<HourlyRemote>,
    val next: String? = null,
    val prev: String? = null
)

/**
 * One Call API 4.0 の GET data/4.0/onecall/timeline/1day のレスポンス。
 * 1レスポンス最大10件。
 */
data class DailyResponseRemote(
    val timezone: String,
    val data: List<DailyRemote>,
    val next: String? = null,
    val prev: String? = null
)

/**
 * 3エンドポイントのレスポンスを1件にまとめた内部モデル。
 * 4.0 ではAPIが分割されたため、ドメイン変換の入口としてこの型に集約する。
 */
data class WeatherRemote(
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
