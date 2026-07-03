package com.anri.weathercalendarapp.core

import com.anri.weathercalendarapp.calendar.data.repositoryimpl.CalendarPreferencesRepositoryImpl
import com.anri.weathercalendarapp.calendar.data.repositoryimpl.CalendarRepositoryImpl
import com.anri.weathercalendarapp.calendar.data.repositoryimpl.HolidayRepositoryImpl
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarPreferencesRepository
import com.anri.weathercalendarapp.calendar.domain.repository.CalendarRepository
import com.anri.weathercalendarapp.calendar.domain.repository.HolidayRepository
import com.anri.weathercalendarapp.weather.data.repositoryimpl.FavoriteLocationRepositoryImpl
import com.anri.weathercalendarapp.weather.data.repositoryimpl.PlacesRepositoryImpl
import com.anri.weathercalendarapp.weather.data.repositoryimpl.WeatherRepositoryImpl
import com.anri.weathercalendarapp.weather.domain.repository.FavoriteLocationRepository
import com.anri.weathercalendarapp.weather.domain.repository.PlacesRepository
import com.anri.weathercalendarapp.weather.domain.repository.WeatherRepository
import com.anri.weathercalendarapp.widget.data.repository.WidgetRepositoryImpl
import com.anri.weathercalendarapp.widget.domain.repository.WidgetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    abstract fun bindCalendarRepository(
        impl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    abstract fun bindCalendarPreferencesRepository(
        impl: CalendarPreferencesRepositoryImpl
    ): CalendarPreferencesRepository

    @Binds
    abstract fun bindPlacesRepository(
        impl: PlacesRepositoryImpl
    ): PlacesRepository

    @Binds
    abstract fun bindFavoriteLocationRepository(
        impl: FavoriteLocationRepositoryImpl
    ): FavoriteLocationRepository

    @Binds
    abstract fun bindHolidayRepository(
        impl: HolidayRepositoryImpl
    ): HolidayRepository

    @Binds
    abstract fun bindWidgetRepository(
        impl: WidgetRepositoryImpl
    ): WidgetRepository

}
