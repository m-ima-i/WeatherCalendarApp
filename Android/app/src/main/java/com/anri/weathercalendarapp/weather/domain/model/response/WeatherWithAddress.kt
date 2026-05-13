package com.anri.weathercalendarapp.weather.domain.model.response

import com.anri.weathercalendarapp.common.geocoder.Address

data class WeatherWithAddress(
    val weather: Weather,
    val address: Address?
)
