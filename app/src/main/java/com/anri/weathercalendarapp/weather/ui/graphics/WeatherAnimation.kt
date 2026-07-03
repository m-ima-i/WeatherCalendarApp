package com.anri.weathercalendarapp.weather.ui.graphics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anri.weathercalendarapp.weather.presentation.type.WeatherAnimationType

@Composable
fun WeatherAnimation(
    type: WeatherAnimationType,
    modifier: Modifier = Modifier
) {
    val colors = rememberWeatherAnimationColors()

    when (type) {
        WeatherAnimationType.SUNNY -> SunnyAnimation(modifier, colors)
        WeatherAnimationType.CLOUDY -> CloudyAnimation(modifier, colors)
        WeatherAnimationType.CLEAR_NIGHT -> ClearNightAnimation(modifier, colors)
        WeatherAnimationType.PARTLY_CLOUDY_DAY -> PartlyCloudyDayAnimation(modifier, colors)
        WeatherAnimationType.PARTLY_CLOUDY_NIGHT -> PartlyCloudyNightAnimation(modifier, colors)
        WeatherAnimationType.FOG -> FogAnimation(modifier, colors)
        WeatherAnimationType.RAIN -> RainAnimation(modifier, colors)
        WeatherAnimationType.LIGHT_RAIN_DAY -> LightRainDayAnimation(modifier, colors)
        WeatherAnimationType.LIGHT_RAIN_NIGHT -> LightRainNightAnimation(modifier, colors)
        WeatherAnimationType.THUNDERSTORM -> ThunderstormAnimation(modifier, colors)
        WeatherAnimationType.SNOW -> SnowAnimation(modifier, colors)
    }
}
