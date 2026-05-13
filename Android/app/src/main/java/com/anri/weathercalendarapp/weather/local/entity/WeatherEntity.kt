package com.anri.weathercalendarapp.weather.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherEntity(
    @PrimaryKey val id: Int = 0,
    val weatherJson: String,
    val addressJson: String? = null
)
