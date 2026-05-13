package com.anri.weathercalendarapp.calendar.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.anri.weathercalendarapp.calendar.data.datasource.CalendarPreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.calendarPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "calendar_pref")

@Singleton
class CalendarPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarPreferencesDataSource {
    companion object {
        private val KEY_SHOW_HOLIDAYS = booleanPreferencesKey("show_holidays")
        const val DEFAULT_SHOW_HOLIDAYS = true
    }

    override fun getShowHolidays(): Flow<Boolean> = context.calendarPreferencesDataStore.data
        .map { preferences ->
            preferences[KEY_SHOW_HOLIDAYS] ?: DEFAULT_SHOW_HOLIDAYS
        }

    override suspend fun setShowHolidays(value: Boolean) {
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[KEY_SHOW_HOLIDAYS] = value
        }
    }
}
