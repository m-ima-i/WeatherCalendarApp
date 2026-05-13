package com.anri.weathercalendarapp.main.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Toast 表示用メッセージ。@StringRes 経由 or 直接文字列を渡せるよう sealed 化。 */
sealed class ToastMessage {
    data class Resource(@StringRes val resId: Int) : ToastMessage()
    data class Plain(val text: String) : ToastMessage()
}

/**
 * アプリ全体で共有するダイアログ表示状態と Toast 発火イベント。
 * ViewModelから直接@Injectして操作できる。
 * 状態変更は関数経由のみ（外部からの直接代入不可）。
 */
@Singleton
class GlobalUiManager @Inject constructor() {

    /** 位置情報権限 Dialog（初回チェック時のみ表示するアプリ独自 Dialog） */
    var showLocationPermissionDialog: Boolean by mutableStateOf(false)
        private set

    /** GPS Dialog（SettingsClient Resolution が利用不可な端末向けフォールバック） */
    var showGpsDialog: Boolean by mutableStateOf(false)
        private set

    /** Toast 発火イベント */
    private val _toastEvent = MutableSharedFlow<ToastMessage>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<ToastMessage> = _toastEvent.asSharedFlow()

    fun setLocationPermissionDialog(show: Boolean) { showLocationPermissionDialog = show }
    fun setGpsDialog(show: Boolean) { showGpsDialog = show }

    /** Toast 発火（@StringRes 経由）。ノンブロッキング、Singleton から呼び出せる */
    fun emitToast(@StringRes resId: Int) {
        _toastEvent.tryEmit(ToastMessage.Resource(resId))
    }

    /** Toast 発火（生文字列）。例外メッセージなど動的文言用 */
    fun emitToast(text: String) {
        _toastEvent.tryEmit(ToastMessage.Plain(text))
    }
}
