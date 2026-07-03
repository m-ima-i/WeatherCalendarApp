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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun FogAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "fog")

    val drift1 by transition.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift1"
    )

    val drift2 by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = -0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift2"
    )

    val drift3 by transition.animateFloat(
        initialValue = -0.04f,
        targetValue = 0.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift3"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barHeight = h * 0.06f
        val cornerRadius = CornerRadius(barHeight / 2)

        data class FogBar(val y: Float, val width: Float, val drift: Float, val color: Color)

        val bars = listOf(
            FogBar(h * 0.28f, w * 0.7f, drift1, colors.fogBar1),
            FogBar(h * 0.38f, w * 0.85f, drift2, colors.fogBar2),
            FogBar(h * 0.48f, w * 0.6f, drift3, colors.fogBar3),
            FogBar(h * 0.58f, w * 0.8f, drift1, colors.fogBar2),
            FogBar(h * 0.68f, w * 0.65f, drift2, colors.fogBar1)
        )

        bars.forEach { bar ->
            val offsetX = w * bar.drift
            drawRoundRect(
                color = bar.color,
                topLeft = Offset((w - bar.width) / 2 + offsetX, bar.y),
                size = Size(bar.width, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
