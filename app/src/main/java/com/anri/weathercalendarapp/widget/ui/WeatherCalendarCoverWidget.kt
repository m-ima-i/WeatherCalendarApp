package com.anri.weathercalendarapp.widget.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.Display
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.anri.weathercalendarapp.MainActivity
import com.anri.weathercalendarapp.R
import com.anri.weathercalendarapp.weather.presentation.type.WeatherType
import com.anri.weathercalendarapp.widget.di.WidgetEntryPoint
import com.anri.weathercalendarapp.widget.domain.model.WidgetCalendarState
import com.anri.weathercalendarapp.widget.domain.model.WidgetWeatherState
import dagger.hilt.android.EntryPointAccessors

/**
 * カバー画面ウィジェット（Galaxy Z Flip のカバー画面 = Flex Window 用）: 天気 + 30日以内の予定
 * 中ウィジェット（WeatherCalendarWidget）の派生。カバー画面はほぼ正方形のため、左右2分割を上下2分割にしている。
 * 下半分に1列では収まらないため、予定は2列（左列に最大5件、右列に残りの最大5件）で表示する。
 * カバー画面に表示するための指定は res/xml/samsung_weather_calendar_cover_widget_info.xml（display="sub_screen"）。
 */
class WeatherCalendarCoverWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, WidgetEntryPoint::class.java
        )
        val getWeatherStateUseCase = entryPoint.getWidgetWeatherStateUseCase()
        val getCalendarStateUseCase = entryPoint.getWidgetCalendarStateUseCase()

        // 初回の状態は provideGlance 内で resolve し、以降は LaunchedEffect(refreshVersion) で再 resolve する（WeatherCalendarWidget と同じ）
        val initialWeatherState = getWeatherStateUseCase.invoke()
        val initialCalendarState = getCalendarStateUseCase.invoke()
        val initialWeatherBitmap = resolveWeatherBitmap(context, initialWeatherState)

        val launchAction = createMainScreenLaunchAction(context)

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

                LaunchedEffect(refreshVersion) {
                    weatherState = getWeatherStateUseCase.invoke()
                    calendarState = getCalendarStateUseCase.invoke()
                    weatherBitmap = resolveWeatherBitmap(context, weatherState)
                }

                val bgColorProvider = WidgetBackgroundHelper.createBackgroundColorProvider(context, opacity)
                val primaryColorProvider = WidgetBackgroundHelper.createPrimaryColorProvider()
                val onSurfaceVariantColorProvider = WidgetBackgroundHelper.createOnSurfaceVariantColorProvider()
                CoverWidgetContent(weatherState, calendarState, weatherBitmap, launchAction, bgColorProvider, primaryColorProvider, onSurfaceVariantColorProvider, noEventsText, fetchFailedText, calendarTitleText)
            }
        }
    }

    /**
     * カバー画面でタップされてもアプリをメイン画面に開くアクション（Samsung の Flex Window ガイドの launchDisplayId: メイン画面 = 0）。
     * ActivityOptions を渡せる actionStartActivity は Glance 1.1.1 では @ExperimentalGlanceApi のため OptIn する。
     */
    @OptIn(ExperimentalGlanceApi::class)
    private fun createMainScreenLaunchAction(context: Context): Action {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val launchOptions = ActivityOptions.makeBasic()
            .setLaunchDisplayId(Display.DEFAULT_DISPLAY)
            .toBundle()
        return actionStartActivity(launchIntent, actionParametersOf(), launchOptions)
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
private fun CoverWidgetContent(
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
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(bgColorProvider)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(launchAction)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            WeatherSection(weatherState, weatherBitmap, primaryColorProvider)
        }

        Column(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            CalendarSection(calendarState, primaryColorProvider, onSurfaceVariantColorProvider, noEventsText, fetchFailedText, calendarTitleText, splitIntoTwoColumns = true)
        }
    }
}

class WeatherCalendarCoverWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherCalendarCoverWidget()
}
