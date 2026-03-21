package com.template.project.feature.auth.domain

data class PasswordValidationState(
    val hasMinLength: Boolean = false,
    val hasUpperCase: Boolean = false,
    val hasLowerCase: Boolean = false,
    val hasDigit: Boolean = false,
) {
    val isValid: Boolean
        get() = hasMinLength && hasUpperCase && hasLowerCase && hasDigit
}

fun validatePassword(password: String): PasswordValidationState {
    return PasswordValidationState(
        hasMinLength = password.length >= 8,
        hasUpperCase = password.any { it.isUpperCase() },
        hasLowerCase = password.any { it.isLowerCase() },
        hasDigit = password.any { it.isDigit() },
    )
}

/**
 * Validates login input fields.
 */
object AuthValidation {

    private const val MIN_PASSWORD_LENGTH = 6

    data class LoginValidationResult(
        val isUsernameValid: Boolean,
        val isPasswordValid: Boolean,
    )

    fun validateLogin(
        username: String,
        password: String,
    ): LoginValidationResult {
        return LoginValidationResult(
            isUsernameValid = username.isNotBlank(),
            isPasswordValid = password.length >= MIN_PASSWORD_LENGTH,
        )
    }
}

