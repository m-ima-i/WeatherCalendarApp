package com.anri.weathercalendarapp.widget.domain.usecase

import android.content.Context
import android.location.LocationManager
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import com.anri.weathercalendarapp.widget.domain.model.WidgetWeatherState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** ウィジェット用の天気表示状態を生成する（Roomから読み取り） */
class GetWidgetWeatherStateUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): WidgetWeatherState {
        // Roomから天気キャッシュを読み取り
        val weather = weatherRepository.getCachedWeather()
        if (weather != null) {
            val address = weatherRepository.getCachedAddress()
            val cityName = address?.thoroughfare
                ?: address?.subLocality
                ?: address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea
                ?: ""
            return WidgetWeatherState.HasData(weather, cityName)
        }

        // データなし → 位置情報サービスの状態で分岐
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled =
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) return WidgetWeatherState.LocationDisabled
        return WidgetWeatherState.NoData
    }
}
