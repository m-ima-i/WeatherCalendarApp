package com.anri.weathercalendarapp.weather.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anri.weathercalendarapp.weather.local.entity.WeatherEntity

@Dao
interface WeatherDAO {
    @Query("SELECT * FROM weather_table LIMIT 1")
    suspend fun getWeather(): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherEntity: WeatherEntity)
}
