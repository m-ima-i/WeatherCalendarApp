package com.anri.weathercalendarapp.weather.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.common.view.CustomElevateCared
import com.anri.weathercalendarapp.weather.presentation.type.WeatherFailureType

/**
 * weather!=null（キャッシュあり）時の失敗バナー。CurrentWeatherCard 上部に表示する。
 *
 * - GEOCODER / LOCATION_FAILED / API_* → リフレッシュアイコン（onRefresh、isLoading 時は回転）
 * - LOCATION_PERMISSION_OFF / LOCATION_AND_GPS_OFF / GPS_OFF → 設定ボタン
 *   - 権限>GPS優先: 権限OFFなら onOpenLocationSettings、GPSのみOFFなら onOpenGpsSettings
 */
@Composable
fun WeatherFailureBanner(
    failureType: WeatherFailureType,
    onRefresh: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenGpsSettings: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val messageResId = when (failureType) {
        WeatherFailureType.GEOCODER,
        WeatherFailureType.LOCATION_FAILED,
        WeatherFailureType.API_UNAUTHORIZED,
        WeatherFailureType.API_QUOTA_EXCEEDED,
        WeatherFailureType.API_SERVER_ERROR,
        WeatherFailureType.API_NETWORK_ERROR,
        WeatherFailureType.API_UNKNOWN -> R.string.weather_failure_banner_update_failed
        WeatherFailureType.LOCATION_PERMISSION_OFF,
        WeatherFailureType.LOCATION_AND_GPS_OFF,
        WeatherFailureType.GPS_OFF -> R.string.weather_failure_banner_permission_gps
    }

    val isPermissionGroup = failureType == WeatherFailureType.LOCATION_PERMISSION_OFF ||
            failureType == WeatherFailureType.LOCATION_AND_GPS_OFF ||
            failureType == WeatherFailureType.GPS_OFF

    CustomElevateCared(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(messageResId),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            if (isPermissionGroup) {
                val onSettingsClick: () -> Unit = when (failureType) {
                    WeatherFailureType.LOCATION_PERMISSION_OFF,
                    WeatherFailureType.LOCATION_AND_GPS_OFF -> onOpenLocationSettings
                    else -> onOpenGpsSettings
                }
                Button(onClick = onSettingsClick) {
                    Text(text = stringResource(R.string.weather_failure_banner_settings_button))
                }
            } else {
                val rotation = if (isLoading) {
                    val infiniteTransition = rememberInfiniteTransition(label = "banner-retry-rotation")
                    val angle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "banner-rotation"
                    )
                    angle
                } else {
                    0f
                }
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.weather_failure_banner_refresh_button),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.graphicsLayer { rotationZ = rotation }
                    )
                }
            }
        }
    }
}
