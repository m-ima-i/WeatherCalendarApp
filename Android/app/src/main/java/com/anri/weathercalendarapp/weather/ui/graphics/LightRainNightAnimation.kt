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
fun LightRainNightAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "lightRainNight")

    val fall by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
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

        val moonRadius = size.minDimension * 0.1f
        drawCircle(color = colors.moon, radius = moonRadius, center = Offset(w * 0.3f, h * 0.25f))

        drawCloud(
            cx = w * (0.55f + drift),
            cy = h * 0.4f,
            scale = w * 0.2f,
            color = colors.cloud
        )

        val dropCount = 6
        val dropLength = h * 0.04f
        val dropWidth = size.minDimension * 0.012f
        val startY = h * 0.55f
        val endY = h * 0.85f

        for (i in 0 until dropCount) {
            val x = w * (0.3f + 0.4f * i / dropCount)
            val phase = (fall + i * 0.3f) % 1f
            val y = startY + (endY - startY) * phase

            drawLine(
                color = colors.rain.copy(alpha = 0.5f),
                start = Offset(x, y),
                end = Offset(x, y + dropLength),
                strokeWidth = dropWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
