package com.template.project.feature.settings.domain

import com.template.project.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppPreferences {
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
