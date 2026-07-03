package com.anri.weathercalendarapp.widget.ui

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.anri.weathercalendarapp.widget.data.datasource.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Glance APIを使用したWidgetUpdater実装。
 * ウィジェットクラス（WeatherMiniWidget等）やWidgetOpacityKeyへの依存をUI層に閉じ込める。
 */
class GlanceWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetUpdater {

    override suspend fun updateAllWidgets(opacity: Int) {
        val manager = GlanceAppWidgetManager(context)
        for (id in manager.getGlanceIds(WeatherMiniWidget::class.java)) {
            updateAppWidgetState(context, id) { it[WidgetOpacityKey] = opacity }
        }
        for (id in manager.getGlanceIds(WeatherSmallWidget::class.java)) {
            updateAppWidgetState(context, id) { it[WidgetOpacityKey] = opacity }
        }
        for (id in manager.getGlanceIds(WeatherCalendarWidget::class.java)) {
            updateAppWidgetState(context, id) { it[WidgetOpacityKey] = opacity }
        }
        WeatherMiniWidget().updateAll(context)
        WeatherSmallWidget().updateAll(context)
        WeatherCalendarWidget().updateAll(context)
    }

    override suspend fun updateCalendarWidget(opacity: Int) {
        val manager = GlanceAppWidgetManager(context)
        for (id in manager.getGlanceIds(WeatherCalendarWidget::class.java)) {
            updateAppWidgetState(context, id) { it[WidgetOpacityKey] = opacity }
        }
        WeatherCalendarWidget().updateAll(context)
    }

    override suspend fun refreshCalendarWidget() {
        // Glance 標準パターン: updateAppWidgetState で状態を書き換え、続けて update() を呼ぶ。
        // - 状態 bump (WidgetRefreshVersionKey) で provideContent 内の
        //   LaunchedEffect(refreshVersion) を発火させ最新 Local を再 resolve。
        // - update(context, id) で session が dormant の場合に launcher 側の
        //   再描画スケジュールを即時要求する（状態 bump だけでは遅延するケースへの対処）。
        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(WeatherCalendarWidget::class.java)
        val widget = WeatherCalendarWidget()
        ids.forEach { id ->
            updateAppWidgetState(context, id) { prefs ->
                prefs[WidgetRefreshVersionKey] = (prefs[WidgetRefreshVersionKey] ?: 0L) + 1L
            }
            widget.update(context, id)
        }
    }

    override suspend fun refreshWeatherWidgets() {
        WeatherMiniWidget().updateAll(context)
        WeatherSmallWidget().updateAll(context)
        WeatherCalendarWidget().updateAll(context)
    }
}
