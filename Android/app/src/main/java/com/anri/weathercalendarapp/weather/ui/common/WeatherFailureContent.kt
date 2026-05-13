package com.anri.weathercalendarapp.weather.ui.common

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType

/**
 * Weather=null 時の失敗状態 UI。
 * - isLoading=true: API 呼び出し中、CircularProgressIndicator のみ表示
 * - 位置情報/GPS 由来: テキスト + 設定遷移ボタン（足りていないものだけ表示、中央揃え）
 * - API 由来: エラーコードに応じた失敗テキストのみ表示
 *
 * showSettingsButtons=false の場合、位置情報/GPS 由来でも設定遷移ボタンは表示しない。
 * お気に入り地域詳細のように位置情報/GPS が無関係な画面で使用する。
 */
@Composable
fun WeatherFailureContent(
    failureType: WeatherFailureType?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    showSettingsButtons: Boolean = true,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary
            )
            return@Box
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = failureMessage(failureType),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (showSettingsButtons &&
                (failureType.requiresLocationSettings() || failureType.requiresGpsSettings())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (failureType.requiresLocationSettings()) {
                        Button(onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.fromParts("package", context.packageName, null)
                                )
                            )
                        }) {
                            Text(text = stringResource(R.string.weather_failure_button_location_settings))
                        }
                    }
                    if (failureType.requiresLocationSettings() && failureType.requiresGpsSettings()) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    if (failureType.requiresGpsSettings()) {
                        Button(onClick = {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }) {
                            Text(text = stringResource(R.string.weather_failure_button_gps_settings))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun failureMessage(failureType: WeatherFailureType?): String {
    val resId = when (failureType) {
        WeatherFailureType.LOCATION_PERMISSION_OFF -> R.string.weather_failure_location_off
        WeatherFailureType.GPS_OFF -> R.string.weather_failure_gps_off
        WeatherFailureType.LOCATION_AND_GPS_OFF -> R.string.weather_failure_location_and_gps_off
        WeatherFailureType.API_UNAUTHORIZED -> R.string.weather_failure_api_unauthorized
        WeatherFailureType.API_QUOTA_EXCEEDED -> R.string.weather_failure_api_quota_exceeded
        WeatherFailureType.API_NETWORK_ERROR -> R.string.weather_failure_api_network_error
        WeatherFailureType.API_SERVER_ERROR -> R.string.weather_failure_api_server_error
        // GEOCODER は weather!=null 時にしか発生しないため WeatherFailureBanner 側で扱う
        // LOCATION_FAILED も weather=null 経路に到達したら API_UNKNOWN と同等扱い
        WeatherFailureType.GEOCODER,
        WeatherFailureType.LOCATION_FAILED,
        WeatherFailureType.API_UNKNOWN, null -> R.string.weather_failure_api_unknown
    }
    return stringResource(resId)
}

private fun WeatherFailureType?.requiresLocationSettings(): Boolean {
    return this == WeatherFailureType.LOCATION_PERMISSION_OFF ||
            this == WeatherFailureType.LOCATION_AND_GPS_OFF
}

private fun WeatherFailureType?.requiresGpsSettings(): Boolean {
    return this == WeatherFailureType.GPS_OFF ||
            this == WeatherFailureType.LOCATION_AND_GPS_OFF
}
