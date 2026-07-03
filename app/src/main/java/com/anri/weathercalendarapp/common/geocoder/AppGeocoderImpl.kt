package com.anri.weathercalendarapp.common.geocoder

import android.content.Context
import android.location.Geocoder
import com.anri.weathercalendarapp.common.location.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class AppGeocoderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppGeocoder {

    override suspend fun fetchAddress(location: Location): Result<Address> = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            if (!Geocoder.isPresent()) {
                return@withContext Result.success(Address())
            }

            val address = withTimeoutOrNull(10_000L) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        val first = addresses.firstOrNull()
                        cont.resume(
                            Address(
                                adminArea = first?.adminArea,
                                subAdminArea = first?.subAdminArea,
                                locality = first?.locality,
                                subLocality = first?.subLocality,
                                thoroughfare = first?.thoroughfare,
                            )
                        )
                    }
                }
            } ?: Address()

            Result.success(address)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
