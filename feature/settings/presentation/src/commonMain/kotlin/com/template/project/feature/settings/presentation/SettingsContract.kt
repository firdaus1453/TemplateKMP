package com.template.project.feature.settings.presentation

import com.template.project.feature.settings.domain.model.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

sealed interface SettingsAction {
    data class OnThemeModeChanged(val mode: ThemeMode) : SettingsAction
}
