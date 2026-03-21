# Core Modules Reference

Complete implementation patterns for all core modules.

## core/domain

The domain module contains pure Kotlin — no framework dependencies.

### Source set structure

```
core/domain/src/commonMain/kotlin/com/mycompany/myapp/core/domain/
├── model/
│   └── User.kt
├── result/
│   ├── Result.kt
│   └── DataError.kt
├── pattern/
│   └── SessionStorage.kt
└── util/
    └── CommonFlow.kt
```

### Result.kt — Unified error handling

```kotlin
package com.mycompany.myapp.core.domain.result

typealias RootError = Error

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}

inline fun <T, E : RootError, R> Result<T, E>.map(
    map: (T) -> R
): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E : RootError> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return map { }
}

inline fun <T, E : RootError> Result<T, E>.onSuccess(
    action: (T) -> Unit
): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E : RootError> Result<T, E>.onError(
    action: (E) -> Unit
): Result<T, E> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

typealias EmptyResult<E> = Result<Unit, E>
```

### DataError.kt — Error hierarchy

```kotlin
package com.mycompany.myapp.core.domain.result

sealed interface DataError : Error {

    enum class Network : DataError {
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN
    }
}
```

### SessionStorage.kt — Auth session interface

```kotlin
package com.mycompany.myapp.core.domain.pattern

interface SessionStorage {
    suspend fun getAccessToken(): String?
    suspend fun setAccessToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun setRefreshToken(token: String)
    suspend fun clearSession()
}
```

### User.kt — Core model

```kotlin
package com.mycompany.myapp.core.domain.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String?
)
```

---

## core/data

The data module handles networking, session management, and shared data utilities.

### Source set structure

```
core/data/src/
├── commonMain/kotlin/com/mycompany/myapp/core/data/
│   ├── networking/
│   │   ├── HttpClientFactory.kt
│   │   ├── HttpClientExt.kt
│   │   └── constructUrl.kt
│   ├── session/
│   │   └── DataStoreSessionStorage.kt
│   └── di/
│       └── CoreDataModule.kt
├── androidMain/kotlin/com/mycompany/myapp/core/data/
│   └── networking/
│       └── HttpClientFactory.android.kt
├── iosMain/kotlin/com/mycompany/myapp/core/data/
│   └── networking/
│       └── HttpClientFactory.ios.kt
└── desktopMain/kotlin/com/mycompany/myapp/core/data/
    └── networking/
        └── HttpClientFactory.desktop.kt
```

### HttpClientFactory.kt (expect)

```kotlin
package com.mycompany.myapp.core.data.networking

import io.ktor.client.HttpClient

expect class HttpClientFactory {
    fun create(): HttpClient
}
```

### HttpClientFactory.android.kt

```kotlin
package com.mycompany.myapp.core.data.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual class HttpClientFactory {
    actual fun create(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
                )
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }
}
```

### HttpClientFactory.ios.kt

```kotlin
package com.mycompany.myapp.core.data.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual class HttpClientFactory {
    actual fun create(): HttpClient {
        return HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
                )
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }
}
```

### HttpClientExt.kt — Safe API calls with Result type

```kotlin
package com.mycompany.myapp.core.data.networking

import com.mycompany.myapp.core.domain.result.DataError
import com.mycompany.myapp.core.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

suspend inline fun <reified Response : Any> HttpClient.safeGet(
    urlString: String,
    queryParameters: Map<String, Any?> = emptyMap()
): Result<Response, DataError.Network> {
    return safeCall {
        get {
            url(constructUrl(urlString))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.safePost(
    urlString: String,
    body: Request
): Result<Response, DataError.Network> {
    return safeCall {
        post {
            url(constructUrl(urlString))
            setBody(body)
        }
    }
}

suspend inline fun <reified Response : Any> HttpClient.safeDelete(
    urlString: String,
    queryParameters: Map<String, Any?> = emptyMap()
): Result<Response, DataError.Network> {
    return safeCall {
        delete {
            url(constructUrl(urlString))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        e.printStackTrace()
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        e.printStackTrace()
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        e.printStackTrace()
        return Result.Error(DataError.Network.UNKNOWN)
    }

    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}
```

### constructUrl.kt

```kotlin
package com.mycompany.myapp.core.data.networking

fun constructUrl(path: String): String {
    return when {
        path.contains("://") -> path
        else -> "https://your-api-base-url.com$path"
    }
}
```

### CoreDataModule.kt — Koin DI module

```kotlin
package com.mycompany.myapp.core.data.di

import com.mycompany.myapp.core.data.networking.HttpClientFactory
import com.mycompany.myapp.core.data.session.DataStoreSessionStorage
import com.mycompany.myapp.core.domain.pattern.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    singleOf(::HttpClientFactory)
    single { get<HttpClientFactory>().create() }
    singleOf(::DataStoreSessionStorage).bind<SessionStorage>()
}
```

---

## core/designsystem

Design system with Material3 theming.

### AppTheme.kt

```kotlin
package com.mycompany.myapp.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B72C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = Color(0xFF545F71),
    onSecondary = Color.White,
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5C8FF),
    onPrimary = Color(0xFF00315E),
    primaryContainer = Color(0xFF004884),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFFBCC7DC),
    onSecondary = Color(0xFF263141),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
```

---

## core/presentation

Shared presentation utilities.

### UiText.kt

```kotlin
package com.mycompany.myapp.core.presentation

import org.jetbrains.compose.resources.StringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResourceText(
        val id: StringResource,
        val args: Array<Any> = arrayOf()
    ) : UiText

    companion object
}
```

### DataErrorToUiText.kt

```kotlin
package com.mycompany.myapp.core.presentation

import com.mycompany.myapp.core.domain.result.DataError

fun DataError.toUiText(): UiText {
    return when (this) {
        DataError.Network.REQUEST_TIMEOUT -> UiText.DynamicString("Request timed out. Please try again.")
        DataError.Network.TOO_MANY_REQUESTS -> UiText.DynamicString("Too many requests. Please wait a moment.")
        DataError.Network.NO_INTERNET -> UiText.DynamicString("No internet connection.")
        DataError.Network.SERVER_ERROR -> UiText.DynamicString("Server error. Please try again later.")
        DataError.Network.SERIALIZATION -> UiText.DynamicString("Data parsing error.")
        DataError.Network.UNAUTHORIZED -> UiText.DynamicString("Unauthorized. Please sign in again.")
        DataError.Network.CONFLICT -> UiText.DynamicString("Conflict occurred.")
        DataError.Network.UNKNOWN -> UiText.DynamicString("An unknown error occurred.")
        DataError.Local.DISK_FULL -> UiText.DynamicString("Storage is full.")
        DataError.Local.UNKNOWN -> UiText.DynamicString("An unknown local error occurred.")
    }
}
```

### ObserveAsEvents.kt — One-time event collector

```kotlin
package com.mycompany.myapp.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Composable
fun <T> ObserveAsEvents(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEvent: (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle, key1, key2) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEvent)
            }
        }
    }
}
```
