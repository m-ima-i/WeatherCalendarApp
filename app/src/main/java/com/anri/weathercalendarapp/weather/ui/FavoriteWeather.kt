package com.anri.weathercalendarapp.weather.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anri.weathercalendarapp.weather.domain.model.response.Weather
import com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.weather.presentation.viewmodel.FavoriteListViewModel
import com.anri.weathercalendarapp.weather.ui.common.CurrentWeatherCard
import com.anri.weathercalendarapp.weather.ui.common.HourlyWeatherRow
import com.anri.weathercalendarapp.weather.ui.common.WeatherFailureContent
import com.anri.weathercalendarapp.weather.ui.weather.Daily
import com.anri.weathercalendarapp.weather.ui.weather.DetailMiniCards

/**
 * Figma 99:1520 お気に入り地域詳細画面
 * リスト画面で取得済みの天気をそのまま表示するだけ。API は呼び出さない。
 *
 * weather=null の挙動:
 * - failureType あり → WeatherFailureContent（API 系のみ。設定ボタンは非表示）
 * - failureType なし → ローディング表示（CircularProgressIndicator）
 */
@Composable
fun FavoriteWeatherScreen(
    favoriteListViewModel: FavoriteListViewModel,
    favoriteId: Long,
) {
    val weatherMap by favoriteListViewModel.weatherMap.collectAsStateWithLifecycle()
    val failureMap by favoriteListViewModel.failureMap.collectAsStateWithLifecycle()
    val weather = weatherMap[favoriteId]
    val failureType = failureMap[favoriteId]

    FavoriteWeatherContent(weather = weather, failureType = failureType)
}

@Composable
private fun FavoriteWeatherContent(
    weather: Weather?,
    failureType: WeatherFailureType?,
) {
    if (weather == null) {
        WeatherFailureContent(
            failureType = failureType,
            isLoading = failureType == null,
            showSettingsButtons = false,
        )
        return
    }

    val current = weather.current
    val hourly = weather.hourly
    val daily = weather.daily

    val currentWeatherType = WeatherType.fromId(current?.weather?.firstOrNull()?.icon)
    val feelsLike = current?.feelsLike ?: 0.0
    val humidity = current?.humidity ?: 0
    val windSpeed = current?.windSpeed ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
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
            modifier = Modifier.weight(1f),
            daily = daily,
        )
    }
}
