package com.anri.weathercalendarapp.common.location

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Google Play Services SettingsClient で GPS 設定状態を確認し、Resolution Dialog を起動可能か判定する。
 * Resolvable の場合は MainActivity の IntentSender ランチャー経由で OS 標準の Resolution Dialog を表示する。
 */
@Singleton
class GpsResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    sealed class Result {
        data object AlreadyEnabled : Result()
        data class Resolvable(val exception: ResolvableApiException) : Result()
        data object NotResolvable : Result()
    }

    suspend fun check(): Result = suspendCancellableCoroutine { cont ->
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        ).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        LocationServices.getSettingsClient(context)
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                cont.resume(Result.AlreadyEnabled)
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException &&
                    e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED
                ) {
                    cont.resume(Result.Resolvable(e))
                } else if (e is ApiException &&
                    e.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE
                ) {
                    cont.resume(Result.NotResolvable)
                } else {
                    cont.resume(Result.NotResolvable)
                }
            }
    }
}
