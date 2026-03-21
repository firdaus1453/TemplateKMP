package com.template.project.core.presentation

import com.template.project.core.domain.result.DataError

fun DataError.toUiText(): UiText {
    return when (this) {
        DataError.Network.REQUEST_TIMEOUT -> UiText.DynamicString("Request timed out. Please try again.")
        DataError.Network.UNAUTHORIZED -> UiText.DynamicString("Unauthorized. Please login again.")
        DataError.Network.CONFLICT -> UiText.DynamicString("Conflict. The data has been modified.")
        DataError.Network.TOO_MANY_REQUESTS -> UiText.DynamicString("Too many requests. Please wait a moment.")
        DataError.Network.NO_INTERNET -> UiText.DynamicString("No internet connection. Please check your network.")
        DataError.Network.SERVER_ERROR -> UiText.DynamicString("Server error. Please try again later.")
        DataError.Network.SERIALIZATION -> UiText.DynamicString("Data parsing error. Please try again.")
        DataError.Network.UNKNOWN -> UiText.DynamicString("An unknown error occurred. Please try again.")
        DataError.Local.DISK_FULL -> UiText.DynamicString("Disk full. Please free up space.")
        DataError.Local.UNKNOWN -> UiText.DynamicString("An unknown local error occurred.")
    }
}
