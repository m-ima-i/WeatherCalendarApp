package com.anri.weathercalendarapp.weather.presentation.state

import com.anri.weathercalendarapp.common.geocoder.Address
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType

data class WeatherUiState(
    // 天気予報データ
    val weather: Weather? = null,

    // 現在地(地名)
    val currentAddress: Address = Address(),

    // weather=null 時の失敗理由（null=未判定/初回ロード前）
    val failureType: WeatherFailureType? = null,

    // 天気APIを呼び出し中かどうか（weather=null + isLoading=true で Indicator 表示）
    val isLoading: Boolean = false,
)
