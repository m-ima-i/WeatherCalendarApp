package com.anri.weathercalendarapp.main.presentation.state

/**
 * 起動フローの進行ステップ。
 * LOAD_LOCAL_WEATHER: Local から天気を読み込む初期ステップ。
 * API_PROCESSES: 天気APIプロセス と カレンダーAPIプロセス を並列実行。
 * COMPLETED: 両プロセスが完了した時点で遷移する終了ステップ。
 */
enum class StartupStep {
    LOAD_LOCAL_WEATHER,
    API_PROCESSES,
    COMPLETED
}

data class MainUiState(
    val startupStep: StartupStep = StartupStep.LOAD_LOCAL_WEATHER,
    val weatherProcessCompleted: Boolean = false,
    val calendarProcessCompleted: Boolean = false
)
