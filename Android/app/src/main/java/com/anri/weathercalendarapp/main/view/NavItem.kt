package com.anri.weathercalendarapp.main.view

data class NavItem(
    val route: Any,
    val iconType: NavIconType,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)
