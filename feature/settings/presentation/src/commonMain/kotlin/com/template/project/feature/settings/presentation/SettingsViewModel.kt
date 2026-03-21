package com.template.project.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.project.feature.settings.domain.AppPreferences
import com.template.project.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state
        .onStart { observeTheme() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnThemeModeChanged -> {
                viewModelScope.launch {
                    appPreferences.setThemeMode(action.mode)
                }
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            appPreferences.observeThemeMode().collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
    }
}
