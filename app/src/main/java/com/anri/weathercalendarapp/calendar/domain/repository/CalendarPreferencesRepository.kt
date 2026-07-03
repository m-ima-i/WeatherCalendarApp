package com.anri.weathercalendarapp.calendar.domain.repository

import kotlinx.coroutines.flow.Flow

interface CalendarPreferencesRepository {
    /** 祝日表示設定をDataStoreからFlowで取得する */
    fun getShowHolidays(): Flow<Boolean>

    /** 祝日表示設定をDataStoreに保存する */
    suspend fun setShowHolidays(value: Boolean)
}
