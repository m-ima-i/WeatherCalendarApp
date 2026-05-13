package com.anri.weathercalendarapp.common.network

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 機内モード時のオフライン早期判定。
 *
 * 起動時 / OnResume 時の API 実行前に呼び、機内モード ON かつ WiFi 未接続なら
 * 重い位置取得（FusedLocationProviderClient 10秒タイムアウト）や GMS 認証要求を
 * 待たず、即座にネットワークエラー扱いするための判定。
 *
 * 機内モード OFF、または機内モードでも WiFi が接続されている場合は通常処理に進める。
 */
@Singleton
class NetworkAvailabilityChecker @Inject constructor(
    private val application: Application
) {
    /** 機内モード ON かつ WiFi 未接続なら true。それ以外（通常処理に進む）は false。 */
    fun isOfflineDueToAirplaneMode(): Boolean {
        val airplaneOn = Settings.Global.getInt(
            application.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
        val wifiConnected = isWifiConnected()
        return airplaneOn && !wifiConnected
    }

    private fun isWifiConnected(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
