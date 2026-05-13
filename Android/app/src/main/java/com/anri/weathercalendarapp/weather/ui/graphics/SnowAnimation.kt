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
fun SnowAnimation(
    modifier: Modifier = Modifier,
    colors: WeatherAnimationColors = rememberWeatherAnimationColors()
) {
    val transition = rememberInfiniteTransition(label = "snow")

    val fall by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall"
    )

    val sway by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCloud(cx = w * 0.5f, cy = h * 0.25f, scale = w * 0.24f, color = colors.cloud)

        val flakeCount = 10
        val startY = h * 0.4f
        val endY = h * 0.95f
        val strokeW = size.minDimension * 0.008f

        for (i in 0 until flakeCount) {
            val baseX = w * (0.15f + 0.7f * i / flakeCount)
            val phase = (fall + i * 0.19f) % 1f
            val y = startY + (endY - startY) * phase
            val swayOffset = w * 0.03f * sway * if (i % 2 == 0) 1f else -1f
            val x = baseX + swayOffset
            val flakeSize = size.minDimension * (0.02f + (i % 3) * 0.008f)
            val branchLen = flakeSize * 0.45f

            for (arm in 0 until 6) {
                val angle = Math.toRadians(arm * 60.0)
                val tipX = x + (flakeSize * cos(angle)).toFloat()
                val tipY = y + (flakeSize * sin(angle)).toFloat()

                drawLine(
                    color = colors.snow.copy(alpha = 0.8f),
                    start = Offset(x, y),
                    end = Offset(tipX, tipY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )

                val midX = x + (flakeSize * 0.55f * cos(angle)).toFloat()
                val midY = y + (flakeSize * 0.55f * sin(angle)).toFloat()
                for (branchDir in listOf(-30.0, 30.0)) {
                    val branchAngle = Math.toRadians(arm * 60.0 + branchDir)
                    drawLine(
                        color = colors.snow.copy(alpha = 0.8f),
                        start = Offset(midX, midY),
                        end = Offset(
                            midX + (branchLen * cos(branchAngle)).toFloat(),
                            midY + (branchLen * sin(branchAngle)).toFloat()
                        ),
                        strokeWidth = strokeW * 0.7f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
