package com.anri.weathercalendarapp.common.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_pref")

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        private val KEY_LOCATION_EVALUATED = booleanPreferencesKey("location_evaluated")
        private val KEY_GPS_EVALUATED = booleanPreferencesKey("gps_evaluated")
    }

    /** 連携中のGoogleアカウントメールアドレス */
    val accountEmail: Flow<String?> = context.authDataStore.data
        .map { preferences ->
            preferences[KEY_ACCOUNT_EMAIL]
        }

    /** accountEmailが保存されていれば連携済みと判定 */
    val isCalendarAuthorized: Flow<Boolean> = accountEmail.map { it != null }

    /** 位置情報権限の分岐を一度でも評価したか（true = 二度と Dialog 表示しない） */
    val locationEvaluated: Flow<Boolean> = context.authDataStore.data
        .map { it[KEY_LOCATION_EVALUATED] ?: false }

    /** GPS の分岐を一度でも評価したか（true = 二度と Dialog 表示しない） */
    val gpsEvaluated: Flow<Boolean> = context.authDataStore.data
        .map { it[KEY_GPS_EVALUATED] ?: false }

    suspend fun setLocationEvaluated() {
        context.authDataStore.edit { it[KEY_LOCATION_EVALUATED] = true }
    }

    suspend fun setGpsEvaluated() {
        context.authDataStore.edit { it[KEY_GPS_EVALUATED] = true }
    }

    suspend fun setAccountEmail(email: String?) {
        context.authDataStore.edit { preferences ->
            if (email != null) {
                preferences[KEY_ACCOUNT_EMAIL] = email
            } else {
                preferences.remove(KEY_ACCOUNT_EMAIL)
            }
        }
    }

    /** ログアウト処理: メールアドレスを削除 */
    suspend fun logout() {
        context.authDataStore.edit { preferences ->
            preferences.remove(KEY_ACCOUNT_EMAIL)
        }
    }
}
