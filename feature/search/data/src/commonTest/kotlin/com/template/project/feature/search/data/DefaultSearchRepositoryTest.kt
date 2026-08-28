package com.template.project.feature.search.data

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
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultSearchRepositoryTest {

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
    fun searchReturnsMappedResultsOn200() = runTest {
        val responseJson = """
            {
                "products": [
                    {
                        "id": 1,
                        "title": "iPhone 15",
                        "description": "Latest Apple smartphone",
                        "thumbnail": "https://example.com/thumb.png",
                        "category": "smartphones",
                        "price": 999.0
                    }
                ],
                "total": 1,
                "skip": 0,
                "limit": 10
            }
        """.trimIndent()

        val repository = DefaultSearchRepository(createClient(responseJson))
        val result = repository.search("iPhone")

        assertIs<Result.Success<*>>(result)
        val items = (result as Result.Success).data
        assertEquals(1, items.size)
        assertEquals("iPhone 15", items[0].title)
        assertEquals(999.0, items[0].price)
    }

    @Test
    fun searchReturnsEmptyListWhenNoMatches() = runTest {
        val responseJson = """
            {
                "products": [],
                "total": 0,
                "skip": 0,
                "limit": 10
            }
        """.trimIndent()

        val repository = DefaultSearchRepository(createClient(responseJson))
        val result = repository.search("nonexistent")

        assertIs<Result.Success<*>>(result)
        val items = (result as Result.Success).data
        assertEquals(0, items.size)
    }

    @Test
    fun searchReturnsNetworkErrorOn500() = runTest {
        val repository = DefaultSearchRepository(createClient("{}", HttpStatusCode.InternalServerError))
        val result = repository.search("test")

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.SERVER_ERROR, result.error)
    }
}
