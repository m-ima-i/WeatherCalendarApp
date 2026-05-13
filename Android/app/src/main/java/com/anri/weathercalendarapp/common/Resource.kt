package com.anri.weathercalendarapp.common

sealed class Resource<T>(
    val data: T? = null
) {
    class Success<T>(data: T): Resource<T>(data)

    class Error<T>(data: T? = null, val cause: Throwable? = null): Resource<T>(data)

    class Loading<T>(data: T? = null): Resource<T>(data)
}
