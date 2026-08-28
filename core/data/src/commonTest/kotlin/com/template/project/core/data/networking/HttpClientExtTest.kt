package com.template.project.core.data.networking

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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HttpClientExtTest {

    @Serializable
    data class TestModel(val id: Int, val name: String)

    private fun createMockClient(
        responseBody: String,
        statusCode: HttpStatusCode,
    ): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
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
    fun safeGetReturnsSuccessOn200() = runTest {
        val client = createMockClient("""{"id": 1, "name": "Test"}""", HttpStatusCode.OK)
        val result: Result<TestModel, DataError.Network> = client.safeGet("/test")

        assertIs<Result.Success<TestModel>>(result)
        assertEquals(1, result.data.id)
        assertEquals("Test", result.data.name)
    }

    @Test
    fun safePostReturnsSuccessOn201() = runTest {
        val client = createMockClient("""{"id": 2, "name": "Created"}""", HttpStatusCode.Created)
        val result: Result<TestModel, DataError.Network> = client.safePost("/test", TestModel(2, "Created"))

        assertIs<Result.Success<TestModel>>(result)
        assertEquals(2, result.data.id)
    }

    @Test
    fun safePutReturnsSuccessOn200() = runTest {
        val client = createMockClient("""{"id": 3, "name": "Updated"}""", HttpStatusCode.OK)
        val result: Result<TestModel, DataError.Network> = client.safePut("/test", TestModel(3, "Updated"))

        assertIs<Result.Success<TestModel>>(result)
        assertEquals("Updated", result.data.name)
    }

    @Test
    fun safeDeleteReturnsSuccessOn200() = runTest {
        val client = createMockClient("""{"id": 4, "name": "Deleted"}""", HttpStatusCode.OK)
        val result: Result<TestModel, DataError.Network> = client.safeDelete("/test")

        assertIs<Result.Success<TestModel>>(result)
        assertEquals(4, result.data.id)
    }

    @Test
    fun safeGetMaps401ToUnauthorized() = runTest {
        val client = createMockClient("{}", HttpStatusCode.Unauthorized)
        val result: Result<TestModel, DataError.Network> = client.safeGet("/test")

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.UNAUTHORIZED, result.error)
    }

    @Test
    fun safeGetMaps408ToRequestTimeout() = runTest {
        val client = createMockClient("{}", HttpStatusCode.RequestTimeout)
        val result: Result<TestModel, DataError.Network> = client.safeGet("/test")

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.REQUEST_TIMEOUT, result.error)
    }

    @Test
    fun safeGetMaps409ToConflict() = runTest {
        val client = createMockClient("{}", HttpStatusCode.Conflict)
        val result: Result<TestModel, DataError.Network> = client.safeGet("/test")

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.CONFLICT, result.error)
    }

    @Test
    fun safeGetMaps429ToTooManyRequests() = runTest {
        val client = createMockClient("{}", HttpStatusCode.TooManyRequests)
        val result: Result<TestModel, DataError.Network> = client.safeGet("/test")

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.TOO_MANY_REQUESTS, result.error)
    }

    @Test
    fun safeGetMaps500ToServerError() = runTest {
        val client = createMockClient("{}", HttpStatusCode.InternalServerError)
        val result: Result<TestModel, DataError.Network> = client.safeGet("/test")

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.SERVER_ERROR, result.error)
    }

    @Test
    fun safeGetMapsOtherStatusToUnknown() = runTest {
        val client = createMockClient("{}", HttpStatusCode.NotFound)
        val result: Result<TestModel, DataError.Network> = client.safeGet("/test")

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.UNKNOWN, result.error)
    }
}
