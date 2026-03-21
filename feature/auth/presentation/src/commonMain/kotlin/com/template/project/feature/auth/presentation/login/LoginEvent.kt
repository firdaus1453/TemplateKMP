package com.template.project.feature.auth.presentation.login

import com.template.project.core.presentation.UiText

sealed interface LoginEvent {
    data class ShowError(val error: UiText) : LoginEvent
    data object LoginSuccess : LoginEvent
}
