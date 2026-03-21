package com.template.project.feature.auth.presentation.login

sealed interface LoginAction {
    data class OnUsernameChanged(val username: String) : LoginAction
    data class OnPasswordChanged(val password: String) : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnLoginClick : LoginAction
    data object OnRegisterClick : LoginAction
}
