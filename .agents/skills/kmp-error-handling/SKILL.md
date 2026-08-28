---
name: kmp-error-handling
description: >
  Chirp error handling conventions, Result<D, E> sealed interface, DataError enums, functional chaining (.map, .onSuccess, .onFailure), and UiText multiplatform localization. Trigger when: creating functions that can fail, defining error types, mapping HTTP/database exceptions to domain errors, or displaying localized errors in UI.
---

# Error Handling & Result Pattern (Chirp Blueprint)

This document defines the strict error handling conventions: typed `Result<D, E>` monads, `RootError` markers, `DataError` taxonomy, functional chaining, and `UiText` multiplatform strings.

---

## ⚠️ The Result Monad (`:core:domain`)

In this architecture, functions never throw business exceptions across layer boundaries. All fallible operations return a typed `Result`:

```kotlin
sealed interface RootError

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>
```

---

## 🔗 Functional Extension Helpers

```kotlin
inline fun <T, E : RootError, R> Result<T, E>.map(
    transform: (T) -> R
): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(transform(data))
    }
}

inline fun <T, E : RootError> Result<T, E>.onSuccess(
    action: (T) -> Unit
): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T, E : RootError> Result<T, E>.onFailure(
    action: (E) -> Unit
): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

fun <T, E : RootError> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return map { }
}
```

---

## 🌐 DataError Taxonomy (`:core:domain`)

```kotlin
sealed interface DataError : RootError {
    enum class Network : DataError {
        BAD_REQUEST,
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
    }

    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        CONSTRAINT_VIOLATION,
        UNKNOWN,
    }
}
```

---

## 💬 Multiplatform UI Text (`UiText`)

Allows domain/presentation layers to specify error strings without Android `Context` or platform-specific APIs:

```kotlin
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResourceId(
        val id: StringResource,
        val args: Array<Any> = emptyArray()
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResourceId -> stringResource(id, *args)
        }
    }
}

// Mapper extension
fun DataError.asUiText(): UiText {
    return when (this) {
        DataError.Network.NO_INTERNET -> UiText.DynamicString("No internet connection available.")
        DataError.Network.REQUEST_TIMEOUT -> UiText.DynamicString("Request timed out. Please try again.")
        DataError.Network.UNAUTHORIZED -> UiText.DynamicString("Invalid credentials or session expired.")
        DataError.Network.SERVER_ERROR -> UiText.DynamicString("Server encountered an error. Please try later.")
        else -> UiText.DynamicString("An unexpected error occurred.")
    }
}
```
