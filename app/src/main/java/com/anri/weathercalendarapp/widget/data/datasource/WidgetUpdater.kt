package com.anri.weathercalendarapp.widget.data.datasource

/**
 * ウィジェットの再描画を抽象化するインターフェース。
 * UI層（Glance）の具象クラスへの依存をData/Presentation層から排除する。
 */
interface WidgetUpdater {
    /** Glance stateにopacityを同期し、全ウィジェット（Mini/Small/Calendar/Cover）を再描画する */
    suspend fun updateAllWidgets(opacity: Int)

    /** Glance stateにopacityを同期し、予定を表示するウィジェット（Calendar/Cover）のみ再描画する */
    suspend fun updateCalendarWidget(opacity: Int)

    /** 予定を表示するウィジェット（Calendar/Cover）を再描画する（opacity同期なし） */
    suspend fun refreshCalendarWidget()

    /** 天気を含む全ウィジェットを再描画する（opacity同期なし） */
    suspend fun refreshWeatherWidgets()
}
