package com.anri.weathercalendarapp.weather.ui.graphics

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun CloudyAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "cloudy")

    val drift1 by transition.animateFloat(
        initialValue = -0.05f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift1"
    )

    val drift2 by transition.animateFloat(
        initialValue = 0.03f,
        targetValue = -0.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift2"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCloud(
            cx = w * (0.55f + drift2),
            cy = h * 0.42f,
            scale = w * 0.22f,
            color = colors.cloud
        )

        drawCloud(
            cx = w * (0.45f + drift1),
            cy = h * 0.52f,
            scale = w * 0.26f,
            color = colors.cloudHighlight
        )
    }
}

/**
 * Figma準拠の雲形状: 楕円ボディ + 3つの円バンプ
 * 全7アニメーションから共有利用
 */
internal fun DrawScope.drawCloud(
    cx: Float,
    cy: Float,
    scale: Float,
    color: Color
) {
    val path = Path().apply {
        addOval(
            Rect(
                left = cx - scale * 0.9f,
                top = cy - scale * 0.25f,
                right = cx + scale * 0.9f,
                bottom = cy + scale * 0.45f
            )
        )
        addOval(
            Rect(
                left = cx - scale * 0.95f,
                top = cy - scale * 0.55f,
                right = cx - scale * 0.05f,
                bottom = cy + scale * 0.35f
            )
        )
        addOval(
            Rect(
                left = cx - scale * 0.5f,
                top = cy - scale * 0.85f,
                right = cx + scale * 0.6f,
                bottom = cy + scale * 0.25f
            )
        )
        addOval(
            Rect(
                left = cx + scale * 0.08f,
                top = cy - scale * 0.47f,
                right = cx + scale * 0.92f,
                bottom = cy + scale * 0.37f
            )
        )
    }
    drawPath(path = path, color = color)
}
