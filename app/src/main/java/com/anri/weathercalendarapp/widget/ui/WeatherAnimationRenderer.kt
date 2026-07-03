package com.anri.weathercalendarapp.widget.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.anri.weathercalendarapp.weather.presentation.type.WeatherAnimationType
import kotlin.math.cos
import kotlin.math.sin

/**
 * WeatherAnimation の初期フレームを Android Canvas で静止画として描画する。
 * Glance ウィジェットでは Compose Composable を直接使えないため、
 * アニメーションの初期状態を Bitmap として生成し ImageProvider で表示する。
 */
object WeatherAnimationRenderer {

    fun render(context: Context, type: WeatherAnimationType, sizeDp: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isDark = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val colors = if (isDark) darkColors() else lightColors()

        when (type) {
            WeatherAnimationType.SUNNY -> drawSunny(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.CLOUDY -> drawCloudy(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.CLEAR_NIGHT -> drawClearNight(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.PARTLY_CLOUDY_DAY -> drawPartlyCloudyDay(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.PARTLY_CLOUDY_NIGHT -> drawPartlyCloudyNight(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.FOG -> drawFog(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.RAIN -> drawRain(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.LIGHT_RAIN_DAY -> drawLightRainDay(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.LIGHT_RAIN_NIGHT -> drawLightRainNight(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.THUNDERSTORM -> drawThunderstorm(canvas, sizePx.toFloat(), colors)
            WeatherAnimationType.SNOW -> drawSnow(canvas, sizePx.toFloat(), colors)
        }

        return bitmap
    }

    private data class IconColors(
        val cloud: Int,
        val cloudHighlight: Int,
        val rain: Int,
        val snow: Int,
        val stars: Int,
        val sunCore: Int,
        val sunRays: Int,
        val moon: Int,
        val lightning: Int,
        val fogBar1: Int,
        val fogBar2: Int,
        val fogBar3: Int
    )

    private fun lightColors() = IconColors(
        cloud = 0xFFCAC4D0.toInt(),       // outlineVariant
        cloudHighlight = 0xFF79747E.toInt(), // outline
        rain = 0xFF6F528A.toInt(),         // primary
        snow = 0xFF49454F.toInt(),         // onSurfaceVariant
        stars = 0xFF49454F.toInt(),
        sunCore = 0xFFFFB300.toInt(),
        sunRays = 0xFFFFF176.toInt(),
        moon = 0xFFFFC107.toInt(),
        lightning = 0xFFFFC107.toInt(),
        fogBar1 = 0x99CAC4D0.toInt(),     // outlineVariant alpha 0.6
        fogBar2 = 0x73CAC4D0.toInt(),     // alpha 0.45
        fogBar3 = 0x4DCAC4D0.toInt()      // alpha 0.3
    )

    private fun darkColors() = IconColors(
        cloud = 0xFFCAC4D0.toInt(),       // onSurfaceVariant
        cloudHighlight = 0xFFE6E0E9.toInt(), // onSurface
        rain = 0xFFDBB9F9.toInt(),         // primary
        snow = 0xFFE6E0E9.toInt(),         // onSurface
        stars = 0xFFE6E0E9.toInt(),
        sunCore = 0xFFFFA726.toInt(),
        sunRays = 0xFFFFD54F.toInt(),
        moon = 0xFFFFD54F.toInt(),
        lightning = 0xFFFFD54F.toInt(),
        fogBar1 = 0x99CAC4D0.toInt(),
        fogBar2 = 0x73CAC4D0.toInt(),
        fogBar3 = 0x4DCAC4D0.toInt()
    )

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun strokePaint(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
    }

    private fun drawCloudShape(canvas: Canvas, cx: Float, cy: Float, scale: Float, color: Int) {
        val paint = fillPaint(color)
        canvas.drawOval(RectF(
            cx - scale * 0.9f, cy - scale * 0.25f,
            cx + scale * 0.9f, cy + scale * 0.45f
        ), paint)
        canvas.drawOval(RectF(
            cx - scale * 0.95f, cy - scale * 0.55f,
            cx - scale * 0.05f, cy + scale * 0.35f
        ), paint)
        canvas.drawOval(RectF(
            cx - scale * 0.5f, cy - scale * 0.85f,
            cx + scale * 0.6f, cy + scale * 0.25f
        ), paint)
        canvas.drawOval(RectF(
            cx + scale * 0.08f, cy - scale * 0.47f,
            cx + scale * 0.92f, cy + scale * 0.37f
        ), paint)
    }

    private fun drawSunny(canvas: Canvas, size: Float, colors: IconColors) {
        val cx = size / 2
        val cy = size / 2
        val coreRadius = size * 0.18f // pulse initial = 0.9 → 0.18 * 0.9 ≈ 0.162, use base

        val rayCount = 12
        val rayLength = size * 0.12f
        val rayStart = coreRadius + size * 0.04f
        val rayPaint = strokePaint(colors.sunRays, size * 0.025f)
        for (i in 0 until rayCount) {
            val angle = Math.toRadians((i * 360.0 / rayCount))
            val startX = cx + (rayStart * cos(angle)).toFloat()
            val startY = cy + (rayStart * sin(angle)).toFloat()
            val endX = cx + ((rayStart + rayLength) * cos(angle)).toFloat()
            val endY = cy + ((rayStart + rayLength) * sin(angle)).toFloat()
            canvas.drawLine(startX, startY, endX, endY, rayPaint)
        }

        canvas.drawCircle(cx, cy, coreRadius, fillPaint(colors.sunCore))
    }

    private fun drawCloudy(canvas: Canvas, size: Float, colors: IconColors) {
        drawCloudShape(canvas, size * 0.55f, size * 0.42f, size * 0.22f, colors.cloud)
        drawCloudShape(canvas, size * 0.45f, size * 0.52f, size * 0.26f, colors.cloudHighlight)
    }

    private fun drawClearNight(canvas: Canvas, size: Float, colors: IconColors) {
        val moonRadius = size * 0.2f
        val moonCx = size * 0.4f
        val moonCy = size * 0.4f

        canvas.drawCircle(moonCx, moonCy, moonRadius, fillPaint(colors.moon))

        val goldenAngle = 137.508f
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * goldenAngle).toDouble())
            val dist = 0.2f + (i * 0.07f) % 0.35f
            val sx = 0.5f + dist * cos(angle).toFloat()
            val sy = 0.5f + dist * sin(angle).toFloat()
            if (sx in 0.25f..0.55f && sy in 0.25f..0.55f) continue
            canvas.drawCircle(size * sx, size * sy, size * 0.015f, fillPaint(colors.stars))
        }
    }

    private fun drawPartlyCloudyDay(canvas: Canvas, size: Float, colors: IconColors) {
        val sunCx = size * 0.35f
        val sunCy = size * 0.35f
        val sunRadius = size * 0.14f

        val rayPaint = strokePaint(colors.sunRays, size * 0.02f)
        val rayLength = size * 0.08f
        val rayStart = sunRadius + size * 0.03f
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 360.0 / 8))
            canvas.drawLine(
                sunCx + (rayStart * cos(angle)).toFloat(),
                sunCy + (rayStart * sin(angle)).toFloat(),
                sunCx + ((rayStart + rayLength) * cos(angle)).toFloat(),
                sunCy + ((rayStart + rayLength) * sin(angle)).toFloat(),
                rayPaint
            )
        }
        canvas.drawCircle(sunCx, sunCy, sunRadius, fillPaint(colors.sunCore))

        drawCloudShape(canvas, size * 0.55f, size * 0.58f, size * 0.22f, colors.cloud)
    }

    private fun drawPartlyCloudyNight(canvas: Canvas, size: Float, colors: IconColors) {
        val starPositions = listOf(0.15f to 0.2f, 0.8f to 0.25f, 0.7f to 0.6f)
        for ((sx, sy) in starPositions) {
            canvas.drawCircle(size * sx, size * sy, size * 0.012f, fillPaint(colors.stars))
        }

        canvas.drawCircle(size * 0.35f, size * 0.35f, size * 0.14f, fillPaint(colors.moon))

        drawCloudShape(canvas, size * 0.55f, size * 0.58f, size * 0.2f, colors.cloud)
    }

    private fun drawFog(canvas: Canvas, size: Float, colors: IconColors) {
        val barHeight = size * 0.06f
        val cornerRadius = barHeight / 2

        data class Bar(val y: Float, val width: Float, val color: Int)

        val bars = listOf(
            Bar(size * 0.28f, size * 0.7f, colors.fogBar1),
            Bar(size * 0.38f, size * 0.85f, colors.fogBar2),
            Bar(size * 0.48f, size * 0.6f, colors.fogBar3),
            Bar(size * 0.58f, size * 0.8f, colors.fogBar2),
            Bar(size * 0.68f, size * 0.65f, colors.fogBar1)
        )

        for (bar in bars) {
            val left = (size - bar.width) / 2
            canvas.drawRoundRect(
                RectF(left, bar.y, left + bar.width, bar.y + barHeight),
                cornerRadius, cornerRadius,
                fillPaint(bar.color)
            )
        }
    }

    private fun drawRain(canvas: Canvas, size: Float, colors: IconColors) {
        drawCloudShape(canvas, size * 0.5f, size * 0.28f, size * 0.24f, colors.cloud)

        val dropLength = size * 0.06f
        val dropPaint = strokePaint(withAlpha(colors.rain, 0.7f), size * 0.015f)
        val startY = size * 0.42f
        val endY = size * 0.9f

        for (i in 0 until 12) {
            val x = size * (0.2f + 0.6f * i / 12)
            val phase = (i * 0.23f) % 1f
            val y = startY + (endY - startY) * phase
            canvas.drawLine(x, y, x - dropLength * 0.1f, y + dropLength, dropPaint)
        }
    }

    private fun drawLightRainDay(canvas: Canvas, size: Float, colors: IconColors) {
        val sunCx = size * 0.3f
        val sunCy = size * 0.25f
        val sunRadius = size * 0.1f

        val rayPaint = strokePaint(colors.sunRays, size * 0.015f)
        val rayLength = size * 0.06f
        val rayStart = sunRadius + size * 0.02f
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 360.0 / 8))
            canvas.drawLine(
                sunCx + (rayStart * cos(angle)).toFloat(),
                sunCy + (rayStart * sin(angle)).toFloat(),
                sunCx + ((rayStart + rayLength) * cos(angle)).toFloat(),
                sunCy + ((rayStart + rayLength) * sin(angle)).toFloat(),
                rayPaint
            )
        }
        canvas.drawCircle(sunCx, sunCy, sunRadius, fillPaint(colors.sunCore))

        drawCloudShape(canvas, size * 0.55f, size * 0.4f, size * 0.2f, colors.cloud)

        val dropLength = size * 0.04f
        val dropPaint = strokePaint(withAlpha(colors.rain, 0.5f), size * 0.012f)
        val startY = size * 0.55f
        val endY = size * 0.85f

        for (i in 0 until 6) {
            val x = size * (0.3f + 0.4f * i / 6)
            val phase = (i * 0.3f) % 1f
            val y = startY + (endY - startY) * phase
            canvas.drawLine(x, y, x, y + dropLength, dropPaint)
        }
    }

    private fun drawLightRainNight(canvas: Canvas, size: Float, colors: IconColors) {
        canvas.drawCircle(size * 0.3f, size * 0.25f, size * 0.1f, fillPaint(colors.moon))

        drawCloudShape(canvas, size * 0.55f, size * 0.4f, size * 0.2f, colors.cloud)

        val dropLength = size * 0.04f
        val dropPaint = strokePaint(withAlpha(colors.rain, 0.5f), size * 0.012f)
        val startY = size * 0.55f
        val endY = size * 0.85f

        for (i in 0 until 6) {
            val x = size * (0.3f + 0.4f * i / 6)
            val phase = (i * 0.3f) % 1f
            val y = startY + (endY - startY) * phase
            canvas.drawLine(x, y, x, y + dropLength, dropPaint)
        }
    }

    private fun drawThunderstorm(canvas: Canvas, size: Float, colors: IconColors) {
        drawCloudShape(canvas, size * 0.5f, size * 0.25f, size * 0.28f, colors.cloud)

        val path = Path().apply {
            moveTo(size * 0.50f, size * 0.38f)
            lineTo(size * 0.47f, size * 0.38f)
            lineTo(size * 0.41f, size * 0.52f)
            lineTo(size * 0.46f, size * 0.52f)
            lineTo(size * 0.40f, size * 0.68f)
            lineTo(size * 0.43f, size * 0.68f)
            lineTo(size * 0.53f, size * 0.50f)
            lineTo(size * 0.48f, size * 0.50f)
            lineTo(size * 0.54f, size * 0.38f)
            close()
        }
        canvas.drawPath(path, fillPaint(colors.lightning))

        val dropLength = size * 0.07f
        val dropPaint = strokePaint(withAlpha(colors.rain, 0.6f), size * 0.015f)
        val startY = size * 0.4f
        val endY = size * 0.95f

        for (i in 0 until 16) {
            val x = size * (0.12f + 0.76f * i / 16)
            val phase = (i * 0.17f) % 1f
            val y = startY + (endY - startY) * phase
            canvas.drawLine(x, y, x - dropLength * 0.15f, y + dropLength, dropPaint)
        }
    }

    private fun drawSnow(canvas: Canvas, size: Float, colors: IconColors) {
        drawCloudShape(canvas, size * 0.5f, size * 0.25f, size * 0.24f, colors.cloud)

        val startY = size * 0.4f
        val endY = size * 0.95f
        val mainStrokeW = size * 0.008f

        for (i in 0 until 10) {
            val baseX = size * (0.15f + 0.7f * i / 10)
            val phase = (i * 0.19f) % 1f
            val y = startY + (endY - startY) * phase
            val swayOffset = size * 0.03f * -1f * if (i % 2 == 0) 1f else -1f
            val x = baseX + swayOffset
            val flakeSize = size * (0.02f + (i % 3) * 0.008f)
            val branchLen = flakeSize * 0.45f
            val snowPaint = strokePaint(withAlpha(colors.snow, 0.8f), mainStrokeW)
            val branchPaint = strokePaint(withAlpha(colors.snow, 0.8f), mainStrokeW * 0.7f)

            for (arm in 0 until 6) {
                val angle = Math.toRadians(arm * 60.0)
                val tipX = x + (flakeSize * cos(angle)).toFloat()
                val tipY = y + (flakeSize * sin(angle)).toFloat()
                canvas.drawLine(x, y, tipX, tipY, snowPaint)

                val midX = x + (flakeSize * 0.55f * cos(angle)).toFloat()
                val midY = y + (flakeSize * 0.55f * sin(angle)).toFloat()
                for (branchDir in listOf(-30.0, 30.0)) {
                    val branchAngle = Math.toRadians(arm * 60.0 + branchDir)
                    canvas.drawLine(
                        midX, midY,
                        midX + (branchLen * cos(branchAngle)).toFloat(),
                        midY + (branchLen * sin(branchAngle)).toFloat(),
                        branchPaint
                    )
                }
            }
        }
    }

    private fun withAlpha(color: Int, alpha: Float): Int {
        val a = (alpha * 255).toInt() and 0xFF
        return (a shl 24) or (color and 0x00FFFFFF)
    }
}
