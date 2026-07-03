package com.anri.weathercalendarapp.widget.ui

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

/** Glanceウィジェットのstate用キー（updateAppWidgetState / currentState で使用） */
val WidgetOpacityKey = intPreferencesKey("widget_opacity")

/**
 * Widget の再描画トリガーとして使う単調増加カウンタ。
 * updateAppWidgetState でこの値を bump すると、currentState<>() を読んでいる
 * provideContent 内の LaunchedEffect(refreshVersion) が再実行され、
 * UseCase 経由で最新の Local 値を取得し直して UI を更新する。
 * Glance の update(context, id) は同一プロセス内で provideGlance を再走させない
 * 仕様のため、状態（Preferences）変更経由で再構成を起こす。
 */
val WidgetRefreshVersionKey = longPreferencesKey("widget_refresh_version")
