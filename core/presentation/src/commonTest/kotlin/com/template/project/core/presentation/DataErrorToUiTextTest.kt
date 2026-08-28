package com.template.project.core.presentation

import com.template.project.core.domain.result.DataError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DataErrorToUiTextTest {

    @Test
    fun dynamicStringAsStringReturnsUnderlyingValue() {
        val uiText = UiText.DynamicString("Hello World")
        assertEquals("Hello World", uiText.asString())
    }

    @Test
    fun allNetworkErrorsMapToMeaningfulUiText() {
        val mappings = mapOf(
            DataError.Network.REQUEST_TIMEOUT to "Request timed out. Please try again.",
            DataError.Network.UNAUTHORIZED to "Unauthorized. Please login again.",
            DataError.Network.CONFLICT to "Conflict. The data has been modified.",
            DataError.Network.TOO_MANY_REQUESTS to "Too many requests. Please wait a moment.",
            DataError.Network.NO_INTERNET to "No internet connection. Please check your network.",
            DataError.Network.SERVER_ERROR to "Server error. Please try again later.",
            DataError.Network.SERIALIZATION to "Data parsing error. Please try again.",
            DataError.Network.UNKNOWN to "An unknown error occurred. Please try again."
        )

        for ((error, expectedMessage) in mappings) {
            val uiText = error.toUiText()
            assertIs<UiText.DynamicString>(uiText)
            assertEquals(expectedMessage, uiText.asString())
        }
    }

    @Test
    fun allLocalErrorsMapToMeaningfulUiText() {
        val mappings = mapOf(
            DataError.Local.DISK_FULL to "Disk full. Please free up space.",
            DataError.Local.UNKNOWN to "An unknown local error occurred."
        )

        for ((error, expectedMessage) in mappings) {
            val uiText = error.toUiText()
            assertIs<UiText.DynamicString>(uiText)
            assertEquals(expectedMessage, uiText.asString())
        }
    }
}
