package com.anri.weathercalendarapp.core

import com.anri.weathercalendarapp.calendar.data.datasource.CalendarLocalDataSource
import com.anri.weathercalendarapp.calendar.data.datasource.CalendarPreferencesDataSource
import com.anri.weathercalendarapp.calendar.data.datasource.CalendarRemoteDataSource
import com.anri.weathercalendarapp.calendar.data.datasource.HolidayRemoteDataSource
import com.anri.weathercalendarapp.calendar.local.datasourceimpl.CalendarLocalDataSourceImpl
import com.anri.weathercalendarapp.calendar.local.datastore.CalendarPreferences
import com.anri.weathercalendarapp.calendar.remote.datasourceimpl.CalendarRemoteDataSourceImpl
import com.anri.weathercalendarapp.calendar.remote.datasourceimpl.HolidayRemoteDataSourceImpl
import com.anri.weathercalendarapp.weather.data.datasource.FavoriteLocationLocalDataSource
import com.anri.weathercalendarapp.weather.data.datasource.WeatherLocalDataSource
import com.anri.weathercalendarapp.weather.data.datasource.WeatherRemoteDataSource
import com.anri.weathercalendarapp.weather.local.datasourceimpl.FavoriteLocationLocalDataSourceImpl
import com.anri.weathercalendarapp.weather.local.datasourceimpl.WeatherLocalDataSourceImpl
import com.anri.weathercalendarapp.weather.remote.datasourceimpl.WeatherRemoteDataSourceImpl
import com.anri.weathercalendarapp.widget.data.datasource.WidgetLocalDataSource
import com.anri.weathercalendarapp.widget.data.datasource.WidgetUpdater
import com.anri.weathercalendarapp.widget.local.datastore.WidgetPreferences
import com.anri.weathercalendarapp.widget.ui.GlanceWidgetUpdater
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindWeatherRemoteDataSource(
        impl: WeatherRemoteDataSourceImpl
    ): WeatherRemoteDataSource

    @Binds
    abstract fun bindWeatherLocalDataSource(
        impl: WeatherLocalDataSourceImpl
    ): WeatherLocalDataSource

    @Binds
    abstract fun bindsCalendarDataSource(
        impl: CalendarRemoteDataSourceImpl
    ): CalendarRemoteDataSource

    @Binds
    abstract fun bindCalendarLocalDataSource(
        impl: CalendarLocalDataSourceImpl
    ): CalendarLocalDataSource

    @Binds
    abstract fun bindCalendarPreferencesDataSource(
        impl: CalendarPreferences
    ): CalendarPreferencesDataSource

    @Binds
    abstract fun bindFavoriteLocationLocalDataSource(
        impl: FavoriteLocationLocalDataSourceImpl
    ): FavoriteLocationLocalDataSource

    @Binds
    abstract fun bindHolidayRemoteDataSource(
        impl: HolidayRemoteDataSourceImpl
    ): HolidayRemoteDataSource

    @Binds
    abstract fun bindWidgetLocalDataSource(
        impl: WidgetPreferences
    ): WidgetLocalDataSource

    @Binds
    abstract fun bindWidgetUpdater(
        impl: GlanceWidgetUpdater
    ): WidgetUpdater

}
