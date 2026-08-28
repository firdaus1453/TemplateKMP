package com.template.project.feature.profile.data

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.profile.data.repository.DefaultProfileRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultProfileRepositoryTest {

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
    fun getCurrentUserReturnsUserOn200() = runTest {
        val responseJson = """
            {
                "id": 1,
                "username": "emilys",
                "email": "emilys@example.com",
                "firstName": "Emily",
                "lastName": "Smith",
                "gender": "female",
                "image": "https://example.com/emily.png",
                "phone": "+123456789",
                "age": 28
            }
        """.trimIndent()

        val repository = DefaultProfileRepository(createClient(responseJson))
        val result = repository.getCurrentUser()

        assertIs<Result.Success<*>>(result)
        val user = (result as Result.Success).data
        assertEquals(1, user.id)
        assertEquals("emilys", user.username)
        assertEquals("Emily", user.firstName)
    }

    @Test
    fun getCurrentUserReturnsUnauthorizedOn401() = runTest {
        val repository = DefaultProfileRepository(createClient("{}", HttpStatusCode.Unauthorized))
        val result = repository.getCurrentUser()

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.UNAUTHORIZED, result.error)
    }
}
