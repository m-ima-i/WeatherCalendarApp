package com.anri.weathercalendarapp.widget.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.anri.weathercalendarapp.MainActivity
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.widget.di.WidgetEntryPoint
import com.anri.weathercalendarapp.widget.domain.model.WidgetWeatherState
import dagger.hilt.android.EntryPointAccessors
import kotlin.math.roundToInt

/**
 * 最小ウィジェット (2x1): タイトル + 天気アイコン + 現在気温
 */
class WeatherMiniWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, WidgetEntryPoint::class.java
        )
        val weatherState = entryPoint.getWidgetWeatherStateUseCase().invoke()

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val launchAction = actionStartActivity(launchIntent)

        val weatherBitmap = if (weatherState is WidgetWeatherState.HasData) {
            val iconCode = weatherState.weather.current.weather.firstOrNull()?.icon
            val weatherType = WeatherType.fromId(iconCode)
            weatherType?.toAnimationType()?.let { animType ->
                WeatherAnimationRenderer.render(context, animType, 40)
            }
        } else null

        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val opacity = prefs[WidgetOpacityKey] ?: 100
                val bgColorProvider = WidgetBackgroundHelper.createBackgroundColorProvider(context, opacity)
                val primaryColorProvider = WidgetBackgroundHelper.createPrimaryColorProvider()
                MiniWidgetContent(weatherState, weatherBitmap, launchAction, bgColorProvider, primaryColorProvider)
            }
        }
    }
}

@Composable
private fun MiniWidgetContent(state: WidgetWeatherState, weatherBitmap: Bitmap?, launchAction: Action, bgColorProvider: ColorProvider, primaryColorProvider: ColorProvider) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(bgColorProvider)
            .padding(16.dp)
            .clickable(launchAction)
    ) {
        when (state) {
            is WidgetWeatherState.LocationDisabled,
            is WidgetWeatherState.NoData -> {
                Text(
                    text = "",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColorProvider
                    )
                )
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "天気の取得に失敗しました。",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.error
                        )
                    )
                }
            }

            is WidgetWeatherState.HasData -> {
                val current = state.weather.current

                Text(
                    text = state.cityName,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColorProvider
                    ),
                    maxLines = 1
                )

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (weatherBitmap != null) {
                            Image(
                                provider = ImageProvider(weatherBitmap),
                                contentDescription = null,
                                modifier = GlanceModifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${current.temp.roundToInt()}°",
                            style = TextStyle(
                                fontSize = 47.sp,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}

class WeatherMiniWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherMiniWidget()
}
