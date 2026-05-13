package com.anri.weathercalendarapp.weather.ui.favorite

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.common.view.helper.TemperatureFormatter
import com.anri.weathercalendarapp.weather.domain.model.response.FavoriteLocationWeatherSummary
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.weather.ui.graphics.WeatherAnimation
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun FavoriteLocationCard(
    summary: FavoriteLocationWeatherSummary,
    onClick: () -> Unit = {},
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val location = summary.favoriteLocation
    val weatherType = WeatherType.fromId(summary.weatherIcon)
    val animationType = weatherType?.toAnimationType()
    val localTime = remember(summary.timezone) {
        try {
            val zone = ZoneId.of(summary.timezone ?: "UTC")
            val now = ZonedDateTime.now(zone)
            now.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: Exception) {
            ""
        }
    }

    val isFailure = summary.failureType != null

    Surface(
        onClick = if (isFailure) onRetry else onClick,
        enabled = !(isFailure && summary.isLoading),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 背景の天気アニメーション（失敗時は非表示）
            if (animationType != null && !isFailure) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                ) {
                    WeatherAnimation(
                        type = animationType,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (localTime.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = location.secondaryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (summary.isLoading && summary.isRetrying) {
                    val infiniteTransition = rememberInfiniteTransition(label = "retry-rotation")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )
                    IconButton(
                        onClick = onRetry,
                        enabled = !summary.isLoading,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.graphicsLayer { rotationZ = rotation }
                        )
                    }
                } else if (summary.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 52.dp),
                        strokeWidth = 2.dp
                    )
                } else if (isFailure) {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (summary.currentTemp != null) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 52.dp)
                    ) {
                        Text(
                            text = TemperatureFormatter.formatTemperature(summary.currentTemp),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (summary.maxTemp != null && summary.minTemp != null) {
                            val maxStr = TemperatureFormatter.formatTemperatureValue(summary.maxTemp)
                            val minStr = TemperatureFormatter.formatTemperatureValue(summary.minTemp)
                            Text(
                                text = "$maxStr / $minStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
