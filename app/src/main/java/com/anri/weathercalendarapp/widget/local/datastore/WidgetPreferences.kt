package com.anri.weathercalendarapp.widget.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anri.weathercalendarapp.widget.data.datasource.WidgetLocalDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_pref")

@Singleton
class WidgetPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetLocalDataSource {
    companion object {
        private val KEY_WIDGET_OPACITY = intPreferencesKey("widget_opacity")
        const val DEFAULT_OPACITY = 100
    }

    override fun getWidgetOpacity(): Flow<Int> = context.widgetDataStore.data
        .map { preferences ->
            preferences[KEY_WIDGET_OPACITY] ?: DEFAULT_OPACITY
        }

    override suspend fun setWidgetOpacity(opacity: Int) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_WIDGET_OPACITY] = opacity.coerceIn(0, 100)
        }
    }
}
