package com.template.project.core.data.auth

import com.template.project.core.domain.auth.AuthInfo
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class DefaultAuthRepositoryTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun createClient(
        responseBody: String,
        statusCode: HttpStatusCode = HttpStatusCode.OK,
    ): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
            engine {
                addHandler {
                    respond(
                        content = responseBody,
                        status = statusCode,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }
    }

    @Test
    fun loginSavesAuthInfoOnSuccess() = runTest {
        val responseJson = """
            {
                "accessToken": "access_xyz",
                "refreshToken": "refresh_xyz",
                "id": 1,
                "username": "emilys",
                "email": "emilys@example.com",
                "firstName": "Emily",
                "lastName": "Smith",
                "image": "https://example.com/emily.png"
            }
        """.trimIndent()

        val fakeSessionStorage = FakeSessionStorage()
        val repository = DefaultAuthRepository(
            httpClient = createClient(responseJson, HttpStatusCode.OK),
            sessionStorage = fakeSessionStorage
        )

        val result = repository.login("emilys", "emilyspass")

        assertIs<Result.Success<Unit>>(result)
        val saved = fakeSessionStorage.observeAuthInfo().first()
        assertEquals("access_xyz", saved?.accessToken)
        assertEquals(1, saved?.userId)
        assertEquals("emilys", saved?.username)
    }

    @Test
    fun loginReturnsErrorOnInvalidCredentials() = runTest {
        val fakeSessionStorage = FakeSessionStorage()
        val repository = DefaultAuthRepository(
            httpClient = createClient("{}", HttpStatusCode.Unauthorized),
            sessionStorage = fakeSessionStorage
        )

        val result = repository.login("wrong", "wrong")

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.UNAUTHORIZED, result.error)
        assertNull(fakeSessionStorage.observeAuthInfo().first())
    }

    @Test
    fun authenticateReturnsUnauthorizedWhenNoSession() = runTest {
        val fakeSessionStorage = FakeSessionStorage()
        val repository = DefaultAuthRepository(
            httpClient = createClient("{}", HttpStatusCode.OK),
            sessionStorage = fakeSessionStorage
        )

        val result = repository.authenticate()

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.UNAUTHORIZED, result.error)
    }

    @Test
    fun authenticateReturnsSuccessWhenSessionExistsAndBackendValidates() = runTest {
        val fakeSessionStorage = FakeSessionStorage()
        fakeSessionStorage.set(
            AuthInfo(
                accessToken = "valid_token",
                refreshToken = "valid_refresh",
                userId = 1,
                username = "emilys",
                email = "emilys@example.com",
                firstName = "Emily",
                lastName = "Smith",
                image = "https://example.com/emily.png"
            )
        )
        val responseJson = """
            {
                "accessToken": "valid_token",
                "refreshToken": "valid_refresh",
                "id": 1,
                "username": "emilys",
                "email": "emilys@example.com",
                "firstName": "Emily",
                "lastName": "Smith",
                "image": "https://example.com/emily.png"
            }
        """.trimIndent()
        val repository = DefaultAuthRepository(
            httpClient = createClient(responseJson, HttpStatusCode.OK),
            sessionStorage = fakeSessionStorage
        )

        val result = repository.authenticate()

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun logoutClearsSessionStorage() = runTest {
        val fakeSessionStorage = FakeSessionStorage()
        fakeSessionStorage.set(
            AuthInfo(
                accessToken = "valid_token",
                refreshToken = "valid_refresh",
                userId = 1,
                username = "emilys",
                email = "emilys@example.com",
                firstName = "Emily",
                lastName = "Smith",
                image = "https://example.com/emily.png"
            )
        )
        val repository = DefaultAuthRepository(
            httpClient = createClient("{}", HttpStatusCode.OK),
            sessionStorage = fakeSessionStorage
        )

        repository.logout()

        assertNull(fakeSessionStorage.observeAuthInfo().first())
    }
}
