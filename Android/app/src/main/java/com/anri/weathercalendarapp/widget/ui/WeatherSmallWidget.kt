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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.anri.weathercalendarapp.MainActivity
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.widget.di.WidgetEntryPoint
import com.anri.weathercalendarapp.widget.domain.model.WidgetWeatherState
import dagger.hilt.android.EntryPointAccessors
import kotlin.math.roundToInt

class WeatherSmallWidget : GlanceAppWidget() {

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
                WeatherAnimationRenderer.render(context, animType, 64)
            }
        } else null

        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val opacity = prefs[WidgetOpacityKey] ?: 100
                val bgColorProvider = WidgetBackgroundHelper.createBackgroundColorProvider(context, opacity)
                val primaryColorProvider = WidgetBackgroundHelper.createPrimaryColorProvider()
                SmallWidgetContent(weatherState, weatherBitmap, launchAction, bgColorProvider, primaryColorProvider)
            }
        }
    }
}

@Composable
private fun SmallWidgetContent(state: WidgetWeatherState, weatherBitmap: Bitmap?, launchAction: Action, bgColorProvider: ColorProvider, primaryColorProvider: ColorProvider) {
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
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
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
                val daily = state.weather.daily.firstOrNull()
                val hourly = state.weather.hourly.firstOrNull()
                val precipChance = ((hourly?.pop?.times(10))?.roundToInt() ?: 0) * 10

                Text(
                    text = state.cityName,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColorProvider
                    ),
                    maxLines = 1
                )

                Box(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                ) {
                    if (weatherBitmap != null) {
                        Image(
                            provider = ImageProvider(weatherBitmap),
                            contentDescription = null,
                            modifier = GlanceModifier.size(100.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Text(
                            text = "${current.temp.roundToInt()}°",
                            style = TextStyle(
                                fontSize = 79.sp,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }
                }

                Row(
                    modifier = GlanceModifier.fillMaxWidth().height(16.dp)
                ) {
                    Box(modifier = GlanceModifier.width(0.dp).defaultWeight().height(16.dp)) {
                        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                            Box(modifier = GlanceModifier.width(12.dp).height(16.dp), contentAlignment = Alignment.Center) {
                                Text(text = "↑", style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.error))
                            }
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(text = "${daily?.temp?.max?.roundToInt() ?: "--"}°", style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant))
                        }
                    }
                    Box(modifier = GlanceModifier.width(0.dp).defaultWeight().height(16.dp)) {
                        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                            Box(modifier = GlanceModifier.width(12.dp).height(16.dp), contentAlignment = Alignment.Center) {
                                Text(text = "↓", style = TextStyle(fontSize = 11.sp, color = primaryColorProvider))
                            }
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(text = "${daily?.temp?.min?.roundToInt() ?: "--"}°", style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant))
                        }
                    }
                    Box(modifier = GlanceModifier.width(0.dp).defaultWeight().height(16.dp)) {}
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth().height(16.dp)
                ) {
                    Box(modifier = GlanceModifier.width(0.dp).defaultWeight().height(16.dp)) {
                        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                            Image(provider = ImageProvider(R.drawable.ic_thermostat), contentDescription = null, modifier = GlanceModifier.size(12.dp))
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(text = "${current.feelsLike.roundToInt()}°", style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant))
                        }
                    }
                    Box(modifier = GlanceModifier.width(0.dp).defaultWeight().height(16.dp)) {
                        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                            Image(provider = ImageProvider(R.drawable.ic_water_drop), contentDescription = null, modifier = GlanceModifier.size(12.dp))
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(text = "${precipChance}%", style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant))
                        }
                    }
                    Box(modifier = GlanceModifier.width(0.dp).defaultWeight().height(16.dp)) {
                        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                            Image(provider = ImageProvider(R.drawable.ic_air), contentDescription = null, modifier = GlanceModifier.size(12.dp))
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(text = "${current.windSpeed.roundToInt()}m/s", style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant))
                        }
                    }
                }
            }
        }
    }
}

class WeatherSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherSmallWidget()
}
