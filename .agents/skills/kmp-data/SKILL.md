---
name: kmp-data
description: >
  Data layer guidelines for Ktor 3.x HTTP networking, HttpClientFactory, offline-first Room caching, SessionStorage, and repository patterns based on Chirp best practices. Trigger when: creating or modifying Ktor network calls, implementing repositories, writing safeCall wrapper utilities, handling Bearer tokens / auto-refresh, or building offline-first sync mechanisms.
---

# Data Layer Patterns (Chirp Networking & Offline-First)

This document defines patterns for data sources, repositories, Ktor 3.x HTTP clients, safe network wrappers, Bearer token refreshment, `SessionStorage`, and offline-first cache strategies.

---

## 🏗️ DataSource vs Repository Pattern

- **Data Source**: Accesses a single source of data (local database or remote API). Owns raw transport, DTO/Entity↔Domain mapping, and exception-to-`DataError` mapping.
  - `LocalProductDataSource`: Manages Room DAOs and database writes.
  - `RemoteProductDataSource`: Executes Ktor HTTP requests and maps DTO responses.
- **Repository**: Coordinates local and remote data sources, implementing the **offline-first single source of truth (SSOT)**.

```
ViewModel ──► ProductRepository (Interface in domain)
                    │
                    ▼
          ProductRepositoryImpl (in data)
          ├──► LocalProductDataSource (Room KMP)
          └──► RemoteProductDataSource (Ktor 3.x)
```

---

## 🌐 Ktor 3.x Setup & `HttpClientFactory` (`core:data`)

The centralized `HttpClientFactory` provides configured Ktor HTTP clients across platforms:

```kotlin
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val sessionStorage: SessionStorage
) {
    fun build(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                    }
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.d(tag = "HttpClient") { message }
                    }
                }
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 20_000
                socketTimeoutMillis = 20_000
            }

            defaultRequest {
                url(BuildKonfig.BASE_URL)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val session = sessionStorage.get()
                        session?.let {
                            BearerTokens(
                                accessToken = it.accessToken,
                                refreshToken = it.refreshToken
                            )
                        }
                    }
                    refreshTokens {
                        // Automatically refresh expired Bearer tokens
                        val session = sessionStorage.get() ?: return@refreshTokens null
                        val response = client.safeCall<RefreshTokenResponse> {
                            post("auth/refresh") {
                                setBody(RefreshTokenRequest(refreshToken = session.refreshToken))
                            }
                        }
                        if (response is Result.Success) {
                            sessionStorage.set(
                                session.copy(
                                    accessToken = response.data.accessToken,
                                    refreshToken = response.data.refreshToken
                                )
                            )
                            BearerTokens(
                                accessToken = response.data.accessToken,
                                refreshToken = response.data.refreshToken
                            )
                        } else {
                            sessionStorage.set(null)
                            null
                        }
                    }
                }
            }
        }
    }
}
```

---

## 🛡️ Safe Network Call Helper (`safeCall`)

Business logic never throws raw network exceptions. All requests are contained by `safeCall`:

```kotlin
suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: HttpRequestTimeoutException) {
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: SerializationException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        return Result.Error(DataError.Network.UNKNOWN)
    }

    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        400 -> Result.Error(DataError.Network.BAD_REQUEST)
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        403 -> Result.Error(DataError.Network.FORBIDDEN)
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}
```

---

## 📴 Offline-First Repository Pattern

```kotlin
class DefaultProductRepository(
    private val remoteDataSource: RemoteProductDataSource,
    private val localDataSource: LocalProductDataSource,
) : ProductRepository {

    override fun observeProducts(): Flow<List<Product>> {
        return localDataSource.observeProducts()
    }

    override suspend fun syncProducts(): EmptyResult<DataError> {
        return when (val result = remoteDataSource.fetchProducts()) {
            is Result.Error -> result.asEmptyResult()
            is Result.Success -> {
                localDataSource.upsertProducts(result.data)
                Result.Success(Unit)
            }
        }
    }
}
```
