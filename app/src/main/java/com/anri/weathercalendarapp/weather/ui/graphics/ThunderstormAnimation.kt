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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun ThunderstormAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "thunderstorm")

    val flash by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flash"
    )

    val fall by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCloud(cx = w * 0.5f, cy = h * 0.25f, scale = w * 0.28f, color = colors.cloud)

        val lightningAlpha = when {
            flash < 0.1f -> flash / 0.1f
            flash < 0.15f -> 1f
            flash < 0.25f -> 1f - (flash - 0.15f) / 0.1f
            flash in 0.3f..0.35f -> (flash - 0.3f) / 0.05f * 0.6f
            flash in 0.35f..0.4f -> 0.6f - (flash - 0.35f) / 0.05f * 0.6f
            else -> 0f
        }

        if (lightningAlpha > 0f) {
            val path = Path().apply {
                moveTo(w * 0.50f, h * 0.38f)
                lineTo(w * 0.47f, h * 0.38f)
                lineTo(w * 0.41f, h * 0.52f)
                lineTo(w * 0.46f, h * 0.52f)
                lineTo(w * 0.40f, h * 0.68f)
                lineTo(w * 0.43f, h * 0.68f)
                lineTo(w * 0.53f, h * 0.50f)
                lineTo(w * 0.48f, h * 0.50f)
                lineTo(w * 0.54f, h * 0.38f)
                close()
            }
            drawPath(
                path = path,
                color = colors.lightning.copy(alpha = lightningAlpha)
            )
        }

        val dropCount = 16
        val dropLength = h * 0.07f
        val dropWidth = size.minDimension * 0.015f
        val startY = h * 0.4f
        val endY = h * 0.95f

        for (i in 0 until dropCount) {
            val x = w * (0.12f + 0.76f * i / dropCount)
            val phase = (fall + i * 0.17f) % 1f
            val y = startY + (endY - startY) * phase

            drawLine(
                color = colors.rain.copy(alpha = 0.6f),
                start = Offset(x, y),
                end = Offset(x - dropLength * 0.15f, y + dropLength),
                strokeWidth = dropWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
