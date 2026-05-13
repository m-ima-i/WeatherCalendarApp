package com.anri.weathercalendarapp.weather.presentation.type

enum class WeatherType(val id: String?) {
    CLEAR_SKY(id = "01d"),
    FEW_CLOUDS(id = "02d"),
    SCATTERED_CLOUDS(id = "03d"),
    BROKEN_CLOUDS(id = "04d"),
    SHOWER_RAIN(id = "09d"),
    RAIN(id = "10d"),
    THUNDERSTORM(id = "11d"),
    SNOW(id = "13d"),
    MIST(id = "50d"),
    CLEAR_SKY_NIGHT(id = "01n"),
    FEW_CLOUDS_NIGHT(id = "02n"),
    SCATTERED_CLOUDS_NIGHT(id = "03n"),
    BROKEN_CLOUDS_NIGHT(id = "04n"),
    SHOWER_RAIN_NIGHT(id = "09n"),
    RAIN_NIGHT(id = "10n"),
    THUNDERSTORM_NIGHT(id = "11n"),
    SNOW_NIGHT(id = "13n"),
    MIST_NIGHT(id = "50n");

    companion object {
        fun fromId(id: String?): WeatherType? {
            return entries.find { it.id == id }
        }
    }

    fun toAnimationType(): WeatherAnimationType {
        return when (this) {
            CLEAR_SKY -> WeatherAnimationType.SUNNY
            FEW_CLOUDS -> WeatherAnimationType.PARTLY_CLOUDY_DAY
            SCATTERED_CLOUDS, BROKEN_CLOUDS -> WeatherAnimationType.CLOUDY
            SHOWER_RAIN -> WeatherAnimationType.LIGHT_RAIN_DAY
            RAIN -> WeatherAnimationType.RAIN
            THUNDERSTORM -> WeatherAnimationType.THUNDERSTORM
            SNOW -> WeatherAnimationType.SNOW
            MIST -> WeatherAnimationType.FOG
            CLEAR_SKY_NIGHT -> WeatherAnimationType.CLEAR_NIGHT
            FEW_CLOUDS_NIGHT -> WeatherAnimationType.PARTLY_CLOUDY_NIGHT
            SCATTERED_CLOUDS_NIGHT, BROKEN_CLOUDS_NIGHT -> WeatherAnimationType.CLOUDY
            SHOWER_RAIN_NIGHT -> WeatherAnimationType.LIGHT_RAIN_NIGHT
            RAIN_NIGHT -> WeatherAnimationType.RAIN
            THUNDERSTORM_NIGHT -> WeatherAnimationType.THUNDERSTORM
            SNOW_NIGHT -> WeatherAnimationType.SNOW
            MIST_NIGHT -> WeatherAnimationType.FOG
        }
    }
}
