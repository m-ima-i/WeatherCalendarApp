package com.anri.weathercalendarapp.common.geocoder

import com.anri.weathercalendarapp.common.location.Location

interface AppGeocoder {
    suspend fun fetchAddress(location: Location): Result<Address>
}
