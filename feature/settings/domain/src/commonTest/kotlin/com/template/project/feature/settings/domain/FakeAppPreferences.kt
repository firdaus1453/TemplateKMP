package com.template.project.feature.settings.domain

import com.template.project.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppPreferences : AppPreferences {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    var setThemeCallCount = 0
        private set

    override fun observeThemeMode(): Flow<ThemeMode> = _themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        setThemeCallCount++
        _themeMode.value = mode
    }
}
