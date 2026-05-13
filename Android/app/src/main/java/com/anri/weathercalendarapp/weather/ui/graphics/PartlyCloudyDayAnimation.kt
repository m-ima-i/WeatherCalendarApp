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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PartlyCloudyDayAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "partlyCloudyDay")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val drift by transition.animateFloat(
        initialValue = -0.03f,
        targetValue = 0.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val sunCx = w * 0.35f
        val sunCy = h * 0.35f
        val sunRadius = size.minDimension * 0.14f

        val rayCount = 8
        val rayLength = size.minDimension * 0.08f
        val rayStart = sunRadius + size.minDimension * 0.03f
        for (i in 0 until rayCount) {
            val angle = Math.toRadians((rotation + i * 360.0 / rayCount).toDouble())
            drawLine(
                color = colors.sunRays,
                start = Offset(
                    sunCx + (rayStart * cos(angle)).toFloat(),
                    sunCy + (rayStart * sin(angle)).toFloat()
                ),
                end = Offset(
                    sunCx + ((rayStart + rayLength) * cos(angle)).toFloat(),
                    sunCy + ((rayStart + rayLength) * sin(angle)).toFloat()
                ),
                strokeWidth = size.minDimension * 0.02f,
                cap = StrokeCap.Round
            )
        }
        drawCircle(color = colors.sunCore, radius = sunRadius, center = Offset(sunCx, sunCy))

        drawCloud(
            cx = w * (0.55f + drift),
            cy = h * 0.58f,
            scale = w * 0.22f,
            color = colors.cloud
        )
    }
}
