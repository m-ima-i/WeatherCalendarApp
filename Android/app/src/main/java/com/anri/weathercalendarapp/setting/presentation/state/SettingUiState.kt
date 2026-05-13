package com.anri.weathercalendarapp.setting.presentation.state

data class SettingUiState(
    val isCalendarAuthorized: Boolean = false,
    val accountEmail: String? = null,
    val widgetOpacity: Int = 100,
    val showHolidays: Boolean = true
)
