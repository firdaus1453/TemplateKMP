# Security Reference

Complete security patterns for KMP/CMP applications.

## 1. HttpClientFactory with Bearer Auth & Token Refresh

The central HTTP client handles authentication automatically via Ktor's `Auth` plugin.

### HttpClientFactory.kt (commonMain)

```kotlin
package com.mycompany.myapp.core.data.networking

import com.mycompany.myapp.core.data.BuildKonfig
import com.mycompany.myapp.core.data.dto.AuthInfoSerializable
import com.mycompany.myapp.core.data.dto.requests.RefreshRequest
import com.mycompany.myapp.core.data.mappers.toDomain
import com.mycompany.myapp.core.domain.auth.SessionStorage
import com.mycompany.myapp.core.domain.logging.AppLogger
import com.mycompany.myapp.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val appLogger: AppLogger,
    private val sessionStorage: SessionStorage,
) {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            // JSON serialization
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
            }

            // Timeouts
            install(HttpTimeout) {
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }

            // Logging — uses multiplatform logger
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        appLogger.debug(message)
                    }
                }
                level = LogLevel.ALL
            }

            // WebSocket support
            install(WebSockets) {
                pingIntervalMillis = 20_000L
            }

            // Default headers: API key + Content-Type
            defaultRequest {
                header("x-api-key", BuildKonfig.API_KEY)
                contentType(ContentType.Application.Json)
            }

            // Bearer authentication with automatic token refresh
            install(Auth) {
                bearer {
                    // Load tokens from session storage on startup
                    loadTokens {
                        sessionStorage
                            .observeAuthInfo()
                            .firstOrNull()
                            ?.let {
                                BearerTokens(
                                    accessToken = it.accessToken,
                                    refreshToken = it.refreshToken
                                )
                            }
                    }

                    // Automatically refresh expired tokens
                    refreshTokens {
                        // Skip refresh for auth endpoints (prevent infinite loop)
                        if (response.request.url.encodedPath.contains("auth/")) {
                            return@refreshTokens null
                        }

                        val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                        if (authInfo?.refreshToken.isNullOrBlank()) {
                            sessionStorage.set(null)
                            return@refreshTokens null
                        }

                        var bearerTokens: BearerTokens? = null
                        client.post<RefreshRequest, AuthInfoSerializable>(
                            route = "/auth/refresh",
                            body = RefreshRequest(
                                refreshToken = authInfo!!.refreshToken
                            ),
                            builder = {
                                markAsRefreshTokenRequest()
                            }
                        ).onSuccess { newAuthInfo ->
                            val newAuthInfoDomain = newAuthInfo.toDomain()
                            sessionStorage.set(newAuthInfoDomain)
                            bearerTokens = BearerTokens(
                                accessToken = newAuthInfoDomain.accessToken,
                                refreshToken = newAuthInfoDomain.refreshToken
                            )
                        }

                        bearerTokens
                    }
                }
            }
        }
    }
}
```

### Key points:
- **API key** is injected via `BuildKonfig` (read from `local.properties`)
- **Bearer tokens** are loaded from `SessionStorage` on first request
- **Token refresh** happens automatically when a 401 is received
- **Auth endpoints are skipped** during refresh to prevent infinite loops
- **Session is cleared** if no refresh token is available

---

## 2. API Key Management with BuildKonfig

### local.properties (root, git-ignored)

```properties
API_KEY=your-api-key-here
BASE_URL=https://api.yourapp.com
```

### BuildKonfigConventionPlugin (reads from local.properties)

```kotlin
class BuildKonfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.codingfeline.buildkonfig")

            extensions.configure<BuildKonfigExtension> {
                packageName = libs.findVersion("projectApplicationId").get().toString()

                val localProperties = Properties()
                val file = rootProject.file("local.properties")
                if (file.exists()) {
                    file.inputStream().use { localProperties.load(it) }
                }

                defaultConfigs {
                    buildConfigField(
                        type = FieldSpec.Type.STRING,
                        name = "API_KEY",
                        value = localProperties.getProperty("API_KEY") ?: ""
                    )
                    buildConfigField(
                        type = FieldSpec.Type.STRING,
                        name = "BASE_URL",
                        value = localProperties.getProperty("BASE_URL") ?: ""
                    )
                }
            }
        }
    }
}
```

### .gitignore (root)

```gitignore
local.properties
google-services.json
GoogleService-Info.plist
*.jks
*.keystore
```

---

## 3. Session Storage with DataStore

### SessionStorage.kt (core/domain — interface)

```kotlin
package com.mycompany.myapp.core.domain.auth

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    fun observeAuthInfo(): Flow<AuthInfo?>
    suspend fun set(authInfo: AuthInfo?)
}
```

### AuthInfo.kt (core/domain)

```kotlin
package com.mycompany.myapp.core.domain.auth

data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)
```

### DataStoreSessionStorage.kt (core/data — implementation)

```kotlin
package com.mycompany.myapp.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mycompany.myapp.core.domain.auth.AuthInfo
import com.mycompany.myapp.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>,
) : SessionStorage {

    companion object {
        private val AUTH_INFO_KEY = stringPreferencesKey("auth_info")
    }

    override fun observeAuthInfo(): Flow<AuthInfo?> {
        return dataStore.data.map { prefs ->
            prefs[AUTH_INFO_KEY]?.let { json ->
                try {
                    Json.decodeFromString<AuthInfoSerializable>(json).toDomain()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun set(authInfo: AuthInfo?) {
        dataStore.edit { prefs ->
            if (authInfo == null) {
                prefs.remove(AUTH_INFO_KEY)
            } else {
                prefs[AUTH_INFO_KEY] = Json.encodeToString(authInfo.toSerializable())
            }
        }
    }
}
```

### DataStore path — expect/actual pattern

```kotlin
// commonMain
package com.mycompany.myapp.core.data.auth
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal const val DATA_STORE_FILE_NAME = "auth_prefs.preferences_pb"

expect fun createDataStore(): DataStore<Preferences>
```

```kotlin
// androidMain
actual fun createDataStore(context: Context): DataStore<Preferences> {
    return createDataStore(
        producePath = {
            context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
        }
    )
}
```

```kotlin
// iosMain
actual fun createDataStore(): DataStore<Preferences> {
    return createDataStore(
        producePath = {
            val dir = NSHomeDirectory() + "/Documents"
            "$dir/$DATA_STORE_FILE_NAME"
        }
    )
}
```

```kotlin
// desktopMain
actual fun createDataStore(): DataStore<Preferences> {
    return createDataStore(
        producePath = {
            val appDir = File(System.getProperty("user.home"), ".myapp")
            if (!appDir.exists()) appDir.mkdirs()
            File(appDir, DATA_STORE_FILE_NAME).absolutePath
        }
    )
}
```

---

## 4. Auth Service Pattern

### AuthService.kt (core/domain — interface)

```kotlin
package com.mycompany.myapp.core.domain.auth

import com.mycompany.myapp.core.domain.util.DataError
import com.mycompany.myapp.core.domain.util.EmptyResult
import com.mycompany.myapp.core.domain.util.Result

interface AuthService {
    suspend fun login(email: String, password: String): Result<AuthInfo, DataError.Remote>
    suspend fun register(email: String, username: String, password: String): EmptyResult<DataError.Remote>
    suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote>
    suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote>
    suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote>
    suspend fun resetPassword(newPassword: String, token: String): EmptyResult<DataError.Remote>
    suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote>
    suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote>
}
```

### KtorAuthService.kt (core/data — implementation)

```kotlin
package com.mycompany.myapp.core.data.auth

import com.mycompany.myapp.core.domain.auth.AuthInfo
import com.mycompany.myapp.core.domain.auth.AuthService
import com.mycompany.myapp.core.domain.util.DataError
import com.mycompany.myapp.core.domain.util.EmptyResult
import com.mycompany.myapp.core.domain.util.Result
import com.mycompany.myapp.core.domain.util.asEmptyResult
import com.mycompany.myapp.core.domain.util.map
import com.mycompany.myapp.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

class KtorAuthService(
    private val httpClient: HttpClient
) : AuthService {

    override suspend fun login(email: String, password: String): Result<AuthInfo, DataError.Remote> {
        return httpClient.post<LoginRequest, AuthInfoSerializable>(
            route = "/auth/login",
            body = LoginRequest(email = email, password = password)
        ).map { it.toDomain() }
    }

    override suspend fun register(email: String, username: String, password: String): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/register",
            body = RegisterRequest(email = email, username = username, password = password)
        )
    }

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> {
        return httpClient.post<RefreshRequest, Unit>(
            route = "/auth/logout",
            body = RefreshRequest(refreshToken)
        ).onSuccess {
            // Clear cached tokens from Ktor Auth plugin
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        }
    }

    // ... other methods follow the same pattern
}
```

---

## 5. Security checklist

- [ ] API keys stored in `local.properties`, never committed to git
- [ ] `local.properties` and `*.jks`/`*.keystore` in `.gitignore`
- [ ] Access token stored securely via DataStore (not SharedPreferences)
- [ ] Token refresh happens automatically on 401
- [ ] Auth endpoints excluded from token refresh to prevent infinite loops
- [ ] `BearerAuthProvider.clearToken()` called on logout
- [ ] Network requests use `x-api-key` header for API key auth
- [ ] ProGuard/R8 rules configured (keep `@Serializable` classes)
- [ ] Firebase config files (`google-services.json`, `GoogleService-Info.plist`) git-ignored
