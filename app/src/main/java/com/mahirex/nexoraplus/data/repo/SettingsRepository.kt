package com.mahirex.nexoraplus.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexora_settings")

data class AppSettings(
    val themeMode: Int = 2,            // 0 light / 1 dark / 2 system
    val languageTag: String = "",      // "" = device default
    val calendarType: Int = 0,         // 0 jalali / 1 gregorian
    val time24: Boolean = true,
    val dailyReminder: Boolean = false,
    val activeAccountId: Long = 0L,
    val occasionVersion: Int = 1
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = intPreferencesKey("theme_mode")
        val LANG = stringPreferencesKey("language_tag")
        val CAL = intPreferencesKey("calendar_type")
        val TIME24 = booleanPreferencesKey("time_24")
        val REMINDER = booleanPreferencesKey("daily_reminder")
        val ACTIVE = longPreferencesKey("active_account")
        val OCC_VERSION = intPreferencesKey("occasion_version")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME] ?: 2,
            languageTag = p[Keys.LANG] ?: "",
            calendarType = p[Keys.CAL] ?: 0,
            time24 = p[Keys.TIME24] ?: true,
            dailyReminder = p[Keys.REMINDER] ?: false,
            activeAccountId = p[Keys.ACTIVE] ?: 0L,
            occasionVersion = p[Keys.OCC_VERSION] ?: 1
        )
    }

    suspend fun setThemeMode(v: Int) = context.dataStore.edit { it[Keys.THEME] = v }
    suspend fun setLanguageTag(v: String) = context.dataStore.edit { it[Keys.LANG] = v }
    suspend fun setCalendarType(v: Int) = context.dataStore.edit { it[Keys.CAL] = v }
    suspend fun setTime24(v: Boolean) = context.dataStore.edit { it[Keys.TIME24] = v }
    suspend fun setDailyReminder(v: Boolean) = context.dataStore.edit { it[Keys.REMINDER] = v }
    suspend fun setActiveAccount(v: Long) = context.dataStore.edit { it[Keys.ACTIVE] = v }
    suspend fun setOccasionVersion(v: Int) = context.dataStore.edit { it[Keys.OCC_VERSION] = v }
}
