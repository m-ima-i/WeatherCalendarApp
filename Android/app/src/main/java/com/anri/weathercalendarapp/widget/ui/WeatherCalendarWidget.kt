package com.anri.weathercalendarapp.widget.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.glance.layout.ColumnScope
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
import com.anri.weathercalendarapp.calendar.domain.model.response.CalendarEvent
import com.anri.weathercalendarapp.calendar.domain.model.response.displayTitle
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.widget.di.WidgetEntryPoint
import com.anri.weathercalendarapp.widget.domain.model.WidgetCalendarState
import com.anri.weathercalendarapp.widget.domain.model.WidgetWeatherState
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.glance.color.ColorProvider as GlanceColorProvider

class WeatherCalendarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, WidgetEntryPoint::class.java
        )
        val getWeatherStateUseCase = entryPoint.getWidgetWeatherStateUseCase()
        val getCalendarStateUseCase = entryPoint.getWidgetCalendarStateUseCase()

        // 初回の状態は provideGlance 内で resolve しておく（最初のレンダリングでの flicker 回避）。
        // 以降の更新は provideContent 内の LaunchedEffect(refreshVersion) で再 resolve する。
        val initialWeatherState = getWeatherStateUseCase.invoke()
        val initialCalendarState = getCalendarStateUseCase.invoke()
        val initialWeatherBitmap = resolveWeatherBitmap(context, initialWeatherState)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val launchAction = actionStartActivity(launchIntent)

        val noEventsText = context.getString(R.string.widget_no_upcoming_events)
        val fetchFailedText = context.getString(R.string.widget_calendar_fetch_failed)
        val calendarTitleText = context.getString(R.string.widget_upcoming_events_title)

        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val opacity = prefs[WidgetOpacityKey] ?: 100
                val refreshVersion = prefs[WidgetRefreshVersionKey] ?: 0L

                var weatherState by remember { mutableStateOf(initialWeatherState) }
                var calendarState by remember { mutableStateOf(initialCalendarState) }
                var weatherBitmap by remember { mutableStateOf(initialWeatherBitmap) }

                // refreshVersion 変化（GlanceWidgetUpdater から bump される）で再 resolve。
                // 同一プロセス内で provideGlance が再走しない Glance 仕様への対処として
                // Preferences 経由のリアクティブ更新で UI を最新化する。
                LaunchedEffect(refreshVersion) {
                    weatherState = getWeatherStateUseCase.invoke()
                    calendarState = getCalendarStateUseCase.invoke()
                    weatherBitmap = resolveWeatherBitmap(context, weatherState)
                }

                val bgColorProvider = WidgetBackgroundHelper.createBackgroundColorProvider(context, opacity)
                val primaryColorProvider = WidgetBackgroundHelper.createPrimaryColorProvider()
                val onSurfaceVariantColorProvider = WidgetBackgroundHelper.createOnSurfaceVariantColorProvider()
                MediumWidgetContent(weatherState, calendarState, weatherBitmap, launchAction, bgColorProvider, primaryColorProvider, onSurfaceVariantColorProvider, noEventsText, fetchFailedText, calendarTitleText)
            }
        }
    }

    private fun resolveWeatherBitmap(context: Context, weatherState: WidgetWeatherState): Bitmap? {
        return if (weatherState is WidgetWeatherState.HasData) {
            val iconCode = weatherState.weather.current.weather.firstOrNull()?.icon
            val weatherType = WeatherType.fromId(iconCode)
            weatherType?.toAnimationType()?.let { animType ->
                WeatherAnimationRenderer.render(context, animType, 64)
            }
        } else null
    }
}

@Composable
private fun MediumWidgetContent(
    weatherState: WidgetWeatherState,
    calendarState: WidgetCalendarState,
    weatherBitmap: Bitmap?,
    launchAction: Action,
    bgColorProvider: ColorProvider,
    primaryColorProvider: ColorProvider,
    onSurfaceVariantColorProvider: ColorProvider,
    noEventsText: String,
    fetchFailedText: String,
    calendarTitleText: String
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(bgColorProvider)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(launchAction)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize().defaultWeight()
                .padding(end = 15.dp)
        ) {
            WeatherSection(weatherState, weatherBitmap, primaryColorProvider)
        }

        Column(
            modifier = GlanceModifier.fillMaxSize().defaultWeight()
        ) {
            CalendarSection(calendarState, primaryColorProvider, onSurfaceVariantColorProvider, noEventsText, fetchFailedText, calendarTitleText)
        }
    }
}

@Composable
private fun ColumnScope.WeatherSection(state: WidgetWeatherState, weatherBitmap: Bitmap?, primaryColorProvider: ColorProvider) {
    when (state) {
        is WidgetWeatherState.LocationDisabled,
        is WidgetWeatherState.NoData -> {
            // 予定側 NotAuthorized と垂直位置を揃えるため、タイトルスロット（空 Text）は置かない
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "天気の取得に失敗しました",
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

            Box(modifier = GlanceModifier.fillMaxWidth().height(10.dp)) {}
        }
    }
}

@Composable
private fun CalendarSection(state: WidgetCalendarState, primaryColorProvider: ColorProvider, onSurfaceVariantColorProvider: ColorProvider, noEventsText: String, fetchFailedText: String, calendarTitleText: String) {
    // NotAuthorized 時はタイトルを非描画にして、天気側のエラー表示と垂直位置を揃える
    if (state !is WidgetCalendarState.NotAuthorized) {
        Text(
            text = calendarTitleText,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColorProvider
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
    }

    when (state) {
        is WidgetCalendarState.NotAuthorized -> {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fetchFailedText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.error
                    )
                )
            }
        }

        is WidgetCalendarState.NoEvents -> {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = noEventsText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }

        is WidgetCalendarState.HasEvents -> {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                state.events.take(7).forEach { event ->
                    EventRow(event, onSurfaceVariantColorProvider)
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent, timeColorProvider: ColorProvider) {
    val (dateText, timeText) = formatEventDateTime(event)
    val barColor = event.backgroundColor?.let {
        try { ComposeColor(AndroidColor.parseColor(it)) }
        catch (_: Exception) { null }
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .width(3.dp)
                .height(20.dp)
                .cornerRadius(2.dp)
                .background(if (barColor != null) GlanceColorProvider(barColor, barColor) else GlanceTheme.colors.primaryContainer)
        ) {}

        Box(modifier = GlanceModifier.width(36.dp).padding(start = 6.dp)) {
            Text(
                text = dateText,
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = timeColorProvider),
                maxLines = 1
            )
        }

        // 時刻 — 固定幅（終日の場合は空欄）
        Box(modifier = GlanceModifier.width(38.dp)) {
            Text(
                text = timeText,
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = timeColorProvider),
                maxLines = 1
            )
        }

        Text(
            text = event.displayTitle(),
            style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurface),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}

/** 日付と時刻を分離して返す。終日イベントの時刻は空文字 */
private fun formatEventDateTime(event: CalendarEvent): Pair<String, String> {
    val start = event.start ?: return Pair("", "")
    return try {
        if (event.isAllDayEvent) {
            val date = LocalDate.parse(start)
            val today = LocalDate.now()
            val dateStr = if (date == today) "今日" else "${date.monthValue}/${date.dayOfMonth}"
            Pair(dateStr, "")
        } else {
            val offsetDateTime = OffsetDateTime.parse(start)
            val today = LocalDate.now()
            val eventDate = offsetDateTime.toLocalDate()
            val dateStr = if (eventDate == today) "今日" else "${eventDate.monthValue}/${eventDate.dayOfMonth}"
            val timeStr = String.format("%02d:%02d", offsetDateTime.hour, offsetDateTime.minute)
            Pair(dateStr, timeStr)
        }
    } catch (_: Exception) {
        Pair("", "")
    }
}

class WeatherCalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherCalendarWidget()
}
