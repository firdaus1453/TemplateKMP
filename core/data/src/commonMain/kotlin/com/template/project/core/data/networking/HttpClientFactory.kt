package com.template.project.core.data.networking

import com.template.project.core.domain.auth.SessionStorage
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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HttpClientFactory(
    private val sessionStorage: SessionStorage,
) {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(HttpTimeout) {
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.d { message }
                    }
                }
                level = LogLevel.ALL
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                        authInfo?.let {
                            BearerTokens(it.accessToken, it.refreshToken)
                        }
                    }
                    refreshTokens {
                        val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null
                        try {
                            val response = client.post(
                                constructUrl("/auth/refresh"),
                            ) {
                                markAsRefreshTokenRequest()
                                setBody(
                                    mapOf("refreshToken" to refreshToken, "expiresInMins" to "30"),
                                )
                            }
                            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                            val newAccessToken = body["accessToken"]?.jsonPrimitive?.content
                                ?: return@refreshTokens null
                            val newRefreshToken = body["refreshToken"]?.jsonPrimitive?.content
                                ?: refreshToken

                            val currentAuth = sessionStorage.observeAuthInfo().firstOrNull()
                                ?: return@refreshTokens null
                            sessionStorage.set(
                                currentAuth.copy(
                                    accessToken = newAccessToken,
                                    refreshToken = newRefreshToken,
                                ),
                            )
                            BearerTokens(newAccessToken, newRefreshToken)
                        } catch (e: Exception) {
                            co.touchlab.kermit.Logger.e(e) { "Token refresh failed" }
                            null
                        }
                    }
                    sendWithoutRequest { request ->
                        // Skip auth for login/register endpoints
                        val path = request.url.pathSegments.joinToString("/")
                        !path.contains("/auth/login") &&
                            !path.contains("/auth/register")
                    }
                }
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}

