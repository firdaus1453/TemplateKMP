package com.template.project.feature.auth.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthValidationTest {

    @Test
    fun validUsernameAndPasswordReturnsNoErrors() {
        val result = AuthValidation.validateLogin(
            username = "emilys",
            password = "emilyspass",
        )
        assertTrue(result.isUsernameValid)
        assertTrue(result.isPasswordValid)
    }

    @Test
    fun emptyUsernameIsInvalid() {
        val result = AuthValidation.validateLogin(
            username = "",
            password = "emilyspass",
        )
        assertFalse(result.isUsernameValid)
        assertTrue(result.isPasswordValid)
    }

    @Test
    fun blankUsernameIsInvalid() {
        val result = AuthValidation.validateLogin(
            username = "   ",
            password = "emilyspass",
        )
        assertFalse(result.isUsernameValid)
    }

    @Test
    fun shortPasswordIsInvalid() {
        val result = AuthValidation.validateLogin(
            username = "emilys",
            password = "abc",
        )
        assertTrue(result.isUsernameValid)
        assertFalse(result.isPasswordValid)
    }

    @Test
    fun emptyPasswordIsInvalid() {
        val result = AuthValidation.validateLogin(
            username = "emilys",
            password = "",
        )
        assertFalse(result.isPasswordValid)
    }

    @Test
    fun exactlyMinLengthPasswordIsValid() {
        val result = AuthValidation.validateLogin(
            username = "emilys",
            password = "abcdef",
        )
        assertTrue(result.isPasswordValid)
    }

    @Test
    fun validatePasswordReturnsValidForStrongPassword() {
        val state = validatePassword("Password123")
        assertTrue(state.hasMinLength)
        assertTrue(state.hasUpperCase)
        assertTrue(state.hasLowerCase)
        assertTrue(state.hasDigit)
        assertTrue(state.isValid)
    }

    @Test
    fun validatePasswordReturnsInvalidForWeakPasswords() {
        val shortState = validatePassword("Pass1")
        assertFalse(shortState.hasMinLength)
        assertFalse(shortState.isValid)

        val noUpper = validatePassword("password123")
        assertFalse(noUpper.hasUpperCase)
        assertFalse(noUpper.isValid)

        val noLower = validatePassword("PASSWORD123")
        assertFalse(noLower.hasLowerCase)
        assertFalse(noLower.isValid)

        val noDigit = validatePassword("PasswordString")
        assertFalse(noDigit.hasDigit)
        assertFalse(noDigit.isValid)
    }
}
