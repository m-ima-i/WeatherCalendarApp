package com.anri.weathercalendarapp.main.presentation.type

import com.anri.weathercalendarapp.main.view.AppRoute

enum class PageType {
    WEATHER,
    HOME,
    CALENDAR;

    companion object{
        fun fromRoute(route: AppRoute): PageType? = when(route) {
            is AppRoute.MainScreen -> HOME
            is AppRoute.Weather -> WEATHER
            is AppRoute.Calendar -> CALENDAR
            else -> null
        }
    }
}