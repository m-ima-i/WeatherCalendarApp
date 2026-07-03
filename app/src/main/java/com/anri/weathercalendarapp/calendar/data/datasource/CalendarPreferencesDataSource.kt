package com.anri.weathercalendarapp.calendar.data.datasource

import kotlinx.coroutines.flow.Flow

interface CalendarPreferencesDataSource {

    /** 祝日表示設定をDataStoreから取得する */
    fun getShowHolidays(): Flow<Boolean>

    /** 祝日表示設定をDataStoreに保存する */
    suspend fun setShowHolidays(value: Boolean)
}
