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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunnyAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "sunny")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val baseRadius = size.minDimension * 0.18f
        val coreRadius = baseRadius * pulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.sunRays.copy(alpha = 0.3f),
                    colors.sunRays.copy(alpha = 0f)
                ),
                center = Offset(cx, cy),
                radius = coreRadius * 2.2f
            ),
            radius = coreRadius * 2.2f,
            center = Offset(cx, cy)
        )

        val rayCount = 12
        val rayLength = size.minDimension * 0.12f
        val rayStart = coreRadius + size.minDimension * 0.04f
        for (i in 0 until rayCount) {
            val angle = Math.toRadians((rotation + i * 360.0 / rayCount).toDouble())
            val startX = cx + (rayStart * cos(angle)).toFloat()
            val startY = cy + (rayStart * sin(angle)).toFloat()
            val endX = cx + ((rayStart + rayLength) * cos(angle)).toFloat()
            val endY = cy + ((rayStart + rayLength) * sin(angle)).toFloat()
            drawLine(
                color = colors.sunRays,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = size.minDimension * 0.025f,
                cap = StrokeCap.Round
            )
        }

        drawCircle(
            color = colors.sunCore,
            radius = coreRadius,
            center = Offset(cx, cy)
        )
    }
}
