package com.template.project.core.domain.auth

/**
 * Cross-cutting logout handler interface.
 * Lives in core:domain because logout is a session concern, not feature-specific.
 * Implemented by auth data layer (AuthRepositoryImpl).
 */
interface LogoutHandler {
    suspend fun logout()
}
