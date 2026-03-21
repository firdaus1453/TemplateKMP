package com.template.project.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.project.core.domain.result.Result
import com.template.project.core.presentation.toUiText
import com.template.project.feature.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart { /* optional init */ }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LoginState(),
        )

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnUsernameChanged -> {
                _state.update {
                    it.copy(
                        username = action.username,
                        canLogin = action.username.isNotBlank() && it.password.isNotBlank(),
                    )
                }
            }
            is LoginAction.OnPasswordChanged -> {
                _state.update {
                    it.copy(
                        password = action.password,
                        canLogin = it.username.isNotBlank() && action.password.isNotBlank(),
                    )
                }
            }
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginAction.OnLoginClick -> login()
            LoginAction.OnRegisterClick -> {
                // Navigation handled by parent
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = authRepository.login(
                state.value.username,
                state.value.password,
            )) {
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.ShowError(result.error.toUiText()))
                }
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.LoginSuccess)
                }
            }
        }
    }
}
