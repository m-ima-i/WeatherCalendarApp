package com.anri.weathercalendarapp.common.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationTrackerImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val application: Application
) : LocationTracker {

    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            application, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            application, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isGpsEnabled(): Boolean {
        val locationManager =
            application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override suspend fun getCurrentLocation(): Location? {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation || !isGpsEnabled()) {
            return null
        }

        val priority = if (hasFineLocation) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        // 精密な位置情報を能動的に取得（10秒でタイムアウト）
        val cancellationTokenSource = CancellationTokenSource()
        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                locationClient.getCurrentLocation(priority, cancellationTokenSource.token)
                    .addOnSuccessListener { location ->
                        cont.resume(
                            location?.let { Location(latitude = it.latitude, longitude = it.longitude) }
                        )
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
                    .addOnCanceledListener {
                        cont.cancel()
                    }
                cont.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        }
    }
}
