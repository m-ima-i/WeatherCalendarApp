package com.anri.weathercalendarapp.common.geocoder

data class Address(
    val adminArea: String? = null,
    val subAdminArea: String? = null,
    val locality: String? = null,
    val subLocality: String? = null,
    val thoroughfare: String? = null,
) {
    fun isEmpty(): Boolean = adminArea == null && subAdminArea == null && locality == null && subLocality == null && thoroughfare == null
}
