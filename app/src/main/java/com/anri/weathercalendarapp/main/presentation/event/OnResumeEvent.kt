package com.anri.weathercalendarapp.main.presentation.event

/**
 * OnResume プロセスの Event。
 * 起動完了後の onResume で、API実行のみ（プロセスではない）を行うトリガー。
 * Dialog 表示・認証要求・権限チェック等の UI 干渉は一切しない。
 */
sealed class OnResumeEvent {
    /** 天気API + カレンダーAPI を独立して実行（成功時のみUI更新、失敗時サイレント） */
    data object RunApis : OnResumeEvent()
}
