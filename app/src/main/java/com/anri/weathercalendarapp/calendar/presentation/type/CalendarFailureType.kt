package com.anri.weathercalendarapp.calendar.presentation.type

import retrofit2.HttpException
import java.io.IOException

/**
 * カレンダー取得・操作失敗の理由種別。
 *
 * - API_UNAUTHORIZED (401): Token 失効。再連携ボタンを表示し再認証を誘導
 * - API_QUOTA_EXCEEDED (429): リクエスト上限超過
 * - API_SERVER_ERROR (5xx): サーバーエラー
 * - API_NETWORK_ERROR (IOException): 通信エラー
 * - API_UNKNOWN: 上記以外の API エラー
 */
enum class CalendarFailureType {
    API_UNAUTHORIZED,
    API_QUOTA_EXCEEDED,
    API_SERVER_ERROR,
    API_NETWORK_ERROR,
    API_UNKNOWN;

    companion object {
        fun fromApiError(cause: Throwable?): CalendarFailureType {
            return when {
                cause is HttpException && cause.code() == 401 -> API_UNAUTHORIZED
                cause is HttpException && cause.code() == 429 -> API_QUOTA_EXCEEDED
                cause is HttpException && cause.code() in 500..599 -> API_SERVER_ERROR
                cause is IOException -> API_NETWORK_ERROR
                else -> API_UNKNOWN
            }
        }
    }
}
