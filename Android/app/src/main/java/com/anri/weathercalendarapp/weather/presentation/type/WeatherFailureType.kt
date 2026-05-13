package com.anri.weathercalendarapp.weather.presentation.type

import retrofit2.HttpException
import java.io.IOException

/**
 * 天気取得失敗の理由種別。
 * weather=null 時は画面中央の WeatherFailureContent、weather!=null 時は
 * CurrentWeatherCard 上部の WeatherFailureBanner にマッピングされる。
 *
 * 権限/GPS 由来:
 * - LOCATION_PERMISSION_OFF / GPS_OFF / LOCATION_AND_GPS_OFF: 設定誘導ボタン表示
 *
 * 位置取得失敗:
 * - LOCATION_FAILED: LocationTracker.getCurrentLocation() が null/例外の時
 *
 * 地名取得失敗:
 * - GEOCODER: 天気API成功 + Geocoder失敗（weather!=null 時のみ発生）
 *
 * API 由来:
 * - API_UNAUTHORIZED (401) / API_QUOTA_EXCEEDED (429) /
 *   API_SERVER_ERROR (5xx) / API_NETWORK_ERROR (IOException系) / API_UNKNOWN
 */
enum class WeatherFailureType {
    LOCATION_PERMISSION_OFF,
    GPS_OFF,
    LOCATION_AND_GPS_OFF,
    LOCATION_FAILED,
    GEOCODER,
    API_UNAUTHORIZED,
    API_QUOTA_EXCEEDED,
    API_SERVER_ERROR,
    API_NETWORK_ERROR,
    API_UNKNOWN;

    companion object {
        /** API 由来の Throwable を WeatherFailureType の API_* にマッピングする */
        fun fromApiError(cause: Throwable?): WeatherFailureType {
            return when {
                cause is HttpException && cause.code() == 401 -> API_UNAUTHORIZED
                cause is HttpException && cause.code() == 429 -> API_QUOTA_EXCEEDED
                cause is HttpException && cause.code() in 500..599 -> API_SERVER_ERROR
                cause is IOException -> API_NETWORK_ERROR
                else -> API_UNKNOWN
            }
        }
    }
}
