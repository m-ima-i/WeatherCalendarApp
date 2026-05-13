package com.anri.weathercalendarapp.common.location

interface LocationTracker {
    suspend fun getCurrentLocation(): Location?
    fun hasLocationPermission(): Boolean
    fun isGpsEnabled(): Boolean
}
