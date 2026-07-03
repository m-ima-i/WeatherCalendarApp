package com.anri.weathercalendarapp.calendar.ui.common

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

/**
 * events!=null（前回取得済み）時のカレンダー失敗バナー。CalendarContent 上部に表示。
 * - 文言: 「カレンダーの更新に失敗しました」（全 failure type 共通）
 * - 右側: リフレッシュアイコン（isLoading 中は回転）
 */
@Composable
fun CalendarFailureBanner(
    onRefresh: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    CustomElevateCared(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.calendar_failure_banner_update_failed),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            val rotation = if (isLoading) {
                val infiniteTransition = rememberInfiniteTransition(label = "calendar-banner-retry-rotation")
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "calendar-banner-rotation"
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
                    contentDescription = stringResource(R.string.calendar_failure_button_refresh),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
        }
    }
}
