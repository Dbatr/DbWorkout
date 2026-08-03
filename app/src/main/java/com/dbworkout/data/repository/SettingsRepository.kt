package com.dbworkout.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

enum class AppThemeMode { SYSTEM, LIGHT, DARK }

class SettingsRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")

    val themeMode: Flow<AppThemeMode> = context.settingsDataStore.data.map { preferences ->
        preferences[themeKey]?.let { stored ->
            AppThemeMode.entries.firstOrNull { it.name == stored }
        } ?: AppThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.settingsDataStore.edit { it[themeKey] = mode.name }
    }
}
