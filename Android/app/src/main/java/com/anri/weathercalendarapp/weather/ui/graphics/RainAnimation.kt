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
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun RainAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "rain")

    val fall by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall"
    )

    val drift by transition.animateFloat(
        initialValue = -0.02f,
        targetValue = 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCloud(
            cx = w * (0.5f + drift),
            cy = h * 0.28f,
            scale = w * 0.24f,
            color = colors.cloud
        )

        val dropCount = 12
        val dropLength = h * 0.06f
        val dropWidth = size.minDimension * 0.015f
        val startY = h * 0.42f
        val endY = h * 0.9f

        for (i in 0 until dropCount) {
            val x = w * (0.2f + 0.6f * i / dropCount)
            val phase = (fall + i * 0.23f) % 1f
            val y = startY + (endY - startY) * phase

            drawLine(
                color = colors.rain.copy(alpha = 0.7f),
                start = Offset(x, y),
                end = Offset(x - dropLength * 0.1f, y + dropLength),
                strokeWidth = dropWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
