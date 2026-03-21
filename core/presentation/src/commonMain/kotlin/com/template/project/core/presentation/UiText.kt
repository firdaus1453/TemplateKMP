package com.template.project.core.presentation

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    fun asString(): String {
        return when (this) {
            is DynamicString -> value
        }
    }
}
