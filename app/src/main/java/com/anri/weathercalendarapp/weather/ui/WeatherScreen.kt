package com.anri.weathercalendarapp.weather.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.weather.presentation.state.WeatherUiState
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.weather.ui.common.CurrentWeatherCard
import com.anri.weathercalendarapp.weather.ui.common.HourlyWeatherRow
import com.anri.weathercalendarapp.weather.ui.common.WeatherFailureContent
import com.anri.weathercalendarapp.weather.ui.weather.Daily
import com.anri.weathercalendarapp.weather.ui.weather.DetailMiniCards

@Composable
fun WeatherScreen(
    weatherUiState: WeatherUiState,
) {
    if (weatherUiState.weather == null) {
        WeatherFailureContent(
            failureType = weatherUiState.failureType,
            isLoading = weatherUiState.isLoading
        )
        return
    }

    val current = weatherUiState.weather.current
    val hourly = weatherUiState.weather.hourly
    val daily = weatherUiState.weather.daily

    val currentWeatherType = WeatherType.fromId(current?.weather?.firstOrNull()?.icon)
    val feelsLike = current?.feelsLike ?: 0.0
    val humidity = current?.humidity ?: 0
    val windSpeed = current?.windSpeed ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        CurrentWeatherCard(
            currentTemp = current?.temp ?: 0.0,
            maxTemp = daily?.firstOrNull()?.temp?.max ?: 0.0,
            minTemp = daily?.firstOrNull()?.temp?.min ?: 0.0,
            feelsLike = feelsLike,
            pop = hourly?.firstOrNull()?.pop ?: 0.0,
            weatherType = currentWeatherType,
        )
        Spacer(modifier = Modifier.height(10.dp))

        HourlyWeatherRow(
            hourlyWeather = hourly,
        )
        Spacer(modifier = Modifier.height(10.dp))

        DetailMiniCards(
            humidity = humidity,
            windSpeed = windSpeed,
        )
        Spacer(modifier = Modifier.height(10.dp))

        Daily(
            daily = daily,
        )
    }
}
