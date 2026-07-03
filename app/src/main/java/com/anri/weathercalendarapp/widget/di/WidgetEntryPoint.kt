package com.anri.weathercalendarapp.widget.di

import com.anri.weathercalendarapp.widget.domain.usecase.GetWidgetCalendarStateUseCase
import com.anri.weathercalendarapp.widget.domain.usecase.GetWidgetWeatherStateUseCase
import com.anri.weathercalendarapp.widget.local.datastore.WidgetPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getWidgetWeatherStateUseCase(): GetWidgetWeatherStateUseCase
    fun getWidgetCalendarStateUseCase(): GetWidgetCalendarStateUseCase
    fun widgetPreferences(): WidgetPreferences
}
