package com.anri.weathercalendarapp.main.presentation.event

import android.app.PendingIntent

sealed class AppScreenUiEvent {
    data class LaunchConsent(val pendingIntent: PendingIntent) : AppScreenUiEvent()
    data object AuthSucceeded : AppScreenUiEvent()
}
