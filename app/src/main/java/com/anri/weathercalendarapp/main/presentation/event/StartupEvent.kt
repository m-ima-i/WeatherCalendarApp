package com.anri.weathercalendarapp.main.presentation.event

/**
 * 起動プロセスの各フローに対応する Event。
 * MainViewModel が次フェーズへの遷移を Event として emit し、
 * UI 側（AppScreen）が collect して該当の処理を実行する。
 */
sealed class StartupEvent {
    /** Localから天気取得 */
    data object LoadLocalWeather : StartupEvent()

    /** 天気APIプロセス */
    data object RunWeatherProcess : StartupEvent()

    /** カレンダーAPIプロセス */
    data object RunCalendarProcess : StartupEvent()
}
