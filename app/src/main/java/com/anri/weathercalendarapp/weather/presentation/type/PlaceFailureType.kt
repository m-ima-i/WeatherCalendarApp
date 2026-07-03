package com.anri.weathercalendarapp.weather.presentation.type

import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.net.PlacesStatusCodes
import java.io.IOException

/**
 * Place 検索失敗の理由種別。Weather/Calendar と同じ5種類分岐の構造。
 *
 * - API_UNAUTHORIZED: REQUEST_DENIED (APIキー無効等)
 * - API_QUOTA_EXCEEDED: OVER_QUERY_LIMIT (クォータ超過)
 * - API_SERVER_ERROR: INVALID_REQUEST / NOT_FOUND / その他 ApiException
 * - API_NETWORK_ERROR: NETWORK_ERROR / IOException 系
 * - API_UNKNOWN: フォールバック
 */
enum class PlaceFailureType {
    API_UNAUTHORIZED,
    API_QUOTA_EXCEEDED,
    API_SERVER_ERROR,
    API_NETWORK_ERROR,
    API_UNKNOWN;

    companion object {
        fun fromApiError(cause: Throwable?): PlaceFailureType {
            val statusCode = (cause as? ApiException)?.statusCode
            return when {
                cause is IOException -> API_NETWORK_ERROR
                statusCode == PlacesStatusCodes.NETWORK_ERROR -> API_NETWORK_ERROR
                statusCode == PlacesStatusCodes.REQUEST_DENIED -> API_UNAUTHORIZED
                statusCode == PlacesStatusCodes.OVER_QUERY_LIMIT -> API_QUOTA_EXCEEDED
                statusCode != null -> API_SERVER_ERROR
                else -> API_UNKNOWN
            }
        }
    }
}
