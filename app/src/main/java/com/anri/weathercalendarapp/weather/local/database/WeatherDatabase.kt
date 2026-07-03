package com.anri.weathercalendarapp.weather.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.anri.weathercalendarapp.calendar.local.dao.CalendarEventDao
import com.anri.weathercalendarapp.calendar.local.entity.CalendarEventEntity
import com.anri.weathercalendarapp.weather.local.dao.FavoriteLocationDAO
import com.anri.weathercalendarapp.weather.local.dao.WeatherDAO
import com.anri.weathercalendarapp.weather.local.entity.FavoriteLocationEntity
import com.anri.weathercalendarapp.weather.local.entity.WeatherEntity

@Database(
    entities = [
        FavoriteLocationEntity::class,
        CalendarEventEntity::class,
        WeatherEntity::class
    ],
    version = 15,
    exportSchema = false
)
abstract class WeatherDatabase: RoomDatabase() {
    abstract fun favoriteLocationDao(): FavoriteLocationDAO
    abstract fun weatherDao(): WeatherDAO
    abstract fun calendarEventDao(): CalendarEventDao
}
