package com.template.project.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.project.core.domain.auth.LogoutHandler
import com.template.project.core.domain.result.Result
import com.template.project.core.presentation.toUiText
import com.template.project.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val logoutHandler: LogoutHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart { loadProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState())

    private val _events = Channel<ProfileEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnRefresh -> loadProfile()
            ProfileAction.OnLogoutClick -> logout()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = profileRepository.getCurrentUser()) {
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(ProfileEvent.ShowError(result.error.toUiText()))
                }
                is Result.Success -> {
                    _state.update { it.copy(user = result.data, isLoading = false) }
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            logoutHandler.logout()
            _events.send(ProfileEvent.LogoutSuccess)
        }
    }
}

