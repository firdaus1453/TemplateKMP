package com.template.project.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.template.project.feature.settings.domain.AppPreferences
import com.template.project.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultAppPreferences(
    private val dataStore: DataStore<Preferences>,
) : AppPreferences {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    override fun observeThemeMode(): Flow<ThemeMode> {
        return dataStore.data.map { prefs ->
            when (prefs[THEME_MODE_KEY]) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }
}
