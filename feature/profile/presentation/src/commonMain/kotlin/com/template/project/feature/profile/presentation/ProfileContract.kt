package com.template.project.feature.profile.presentation

import com.template.project.core.domain.model.User
import com.template.project.core.presentation.UiText

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
)

sealed interface ProfileAction {
    data object OnRefresh : ProfileAction
    data object OnLogoutClick : ProfileAction
}

sealed interface ProfileEvent {
    data object LogoutSuccess : ProfileEvent
    data class ShowError(val error: UiText) : ProfileEvent
}
