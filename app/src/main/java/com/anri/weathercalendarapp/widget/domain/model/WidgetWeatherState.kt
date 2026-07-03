package com.anri.weathercalendarapp.widget.domain.model

import com.anri.weathercalendarapp.weather.domain.model.response.Weather

/** 天気ウィジェットの3状態 */
sealed class WidgetWeatherState {

    /** 位置情報サービスまたはGPSがOFF */
    data object LocationDisabled : WidgetWeatherState()

    /** 位置情報は有効だが天気データがまだない */
    data object NoData : WidgetWeatherState()

    /** 天気データあり */
    data class HasData(
        val weather: Weather,
        val cityName: String
    ) : WidgetWeatherState()
}
