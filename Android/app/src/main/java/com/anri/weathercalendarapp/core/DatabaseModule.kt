package com.anri.weathercalendarapp.core

import android.content.Context
import androidx.room.Room
import com.anri.weathercalendarapp.calendar.local.dao.CalendarEventDao
import com.anri.weathercalendarapp.weather.local.dao.FavoriteLocationDAO
import com.anri.weathercalendarapp.weather.local.dao.WeatherDAO
import com.anri.weathercalendarapp.weather.local.database.WeatherDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(
        @ApplicationContext context: Context
    ): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteLocationDao(database: WeatherDatabase): FavoriteLocationDAO {
        return database.favoriteLocationDao()
    }

    @Provides
    @Singleton
    fun provideCalendarEventDao(database: WeatherDatabase): CalendarEventDao {
        return database.calendarEventDao()
    }

    @Provides
    @Singleton
    fun provideWeatherDao(database: WeatherDatabase): WeatherDAO {
        return database.weatherDao()
    }
}
