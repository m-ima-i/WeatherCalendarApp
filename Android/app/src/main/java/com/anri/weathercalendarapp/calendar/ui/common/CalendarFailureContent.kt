package com.anri.weathercalendarapp.calendar.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.calendar.presentation.type.CalendarFailureType

/**
 * カレンダー失敗状態 UI。
 * - failureType=API_UNAUTHORIZED: 失効テキスト + 再連携ボタン（onRequestReauth）
 * - failureType=API_NETWORK_ERROR / API_SERVER_ERROR / API_UNKNOWN: テキスト + リフレッシュボタン（onRefresh）
 *   - isLoading=true 時はボタン内のテキストを回転する Refresh アイコンに差し替え
 * - failureType=API_QUOTA_EXCEEDED: テキストのみ
 */
@Composable
fun CalendarFailureContent(
    failureType: CalendarFailureType?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onRequestReauth: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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

            when (failureType) {
                CalendarFailureType.API_UNAUTHORIZED -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRequestReauth) {
                        Text(text = stringResource(R.string.calendar_failure_button_reauth))
                    }
                }
                CalendarFailureType.API_NETWORK_ERROR,
                CalendarFailureType.API_SERVER_ERROR,
                CalendarFailureType.API_UNKNOWN -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRefresh,
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            val infiniteTransition =
                                rememberInfiniteTransition(label = "calendar-refresh-rotation")
                            val angle by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "calendar-refresh-angle"
                            )
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.calendar_failure_button_refresh),
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer { rotationZ = angle }
                            )
                        } else {
                            Text(text = stringResource(R.string.calendar_failure_button_refresh))
                        }
                    }
                }
                CalendarFailureType.API_QUOTA_EXCEEDED, null -> {}
            }
        }
    }
}

@Composable
private fun failureMessage(failureType: CalendarFailureType?): String {
    val resId = when (failureType) {
        CalendarFailureType.API_UNAUTHORIZED -> R.string.calendar_failure_api_unauthorized
        CalendarFailureType.API_QUOTA_EXCEEDED -> R.string.calendar_failure_api_quota_exceeded
        CalendarFailureType.API_NETWORK_ERROR -> R.string.calendar_failure_api_network_error
        CalendarFailureType.API_SERVER_ERROR -> R.string.calendar_failure_api_server_error
        CalendarFailureType.API_UNKNOWN, null -> R.string.calendar_failure_api_unknown
    }
    return stringResource(resId)
}
