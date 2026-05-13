package com.anri.weathercalendarapp.widget.data.datasource

/**
 * ウィジェットの再描画を抽象化するインターフェース。
 * UI層（Glance）の具象クラスへの依存をData/Presentation層から排除する。
 */
interface WidgetUpdater {
    /** Glance stateにopacityを同期し、全ウィジェット（Mini/Small/Calendar）を再描画する */
    suspend fun updateAllWidgets(opacity: Int)

    /** Glance stateにopacityを同期し、カレンダーウィジェットのみ再描画する */
    suspend fun updateCalendarWidget(opacity: Int)

    /** カレンダーウィジェットを再描画する（opacity同期なし） */
    suspend fun refreshCalendarWidget()

    /** 天気を含む全ウィジェットを再描画する（opacity同期なし） */
    suspend fun refreshWeatherWidgets()
}
