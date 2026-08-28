package com.template.project.feature.home.data

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.home.data.repository.DefaultProductRepository
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

class DefaultProductRepositoryTest {

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
    fun getProductsReturnsMappedProductListOn200() = runTest {
        val responseJson = """
            {
                "products": [
                    {
                        "id": 1,
                        "title": "Essence Mascara Lash Princess",
                        "description": "The Essence Mascara Lash Princess is a popular mascara.",
                        "category": "beauty",
                        "price": 9.99,
                        "discountPercentage": 7.17,
                        "rating": 4.94,
                        "stock": 5,
                        "brand": "Essence",
                        "thumbnail": "https://cdn.dummyjson.com/products/images/beauty/Essence%20Mascara%20Lash%20Princess/thumbnail.png",
                        "images": ["https://cdn.dummyjson.com/products/images/beauty/Essence%20Mascara%20Lash%20Princess/1.png"]
                    }
                ],
                "total": 1,
                "skip": 0,
                "limit": 10
            }
        """.trimIndent()

        val repository = DefaultProductRepository(createClient(responseJson))
        val result = repository.getProducts(limit = 10, skip = 0)

        assertIs<Result.Success<*>>(result)
        val products = (result as Result.Success).data
        assertEquals(1, products.size)
        assertEquals("Essence Mascara Lash Princess", products[0].title)
        assertEquals(9.99, products[0].price)
    }

    @Test
    fun getProductByIdReturnsProductOn200() = runTest {
        val responseJson = """
            {
                "id": 42,
                "title": "Special Product",
                "description": "Product 42 description",
                "category": "groceries",
                "price": 15.49,
                "discountPercentage": 2.0,
                "rating": 4.5,
                "stock": 12,
                "brand": "Special Brand",
                "thumbnail": "https://example.com/thumb.png",
                "images": []
            }
        """.trimIndent()

        val repository = DefaultProductRepository(createClient(responseJson))
        val result = repository.getProductById(42)

        assertIs<Result.Success<*>>(result)
        val product = (result as Result.Success).data
        assertEquals(42, product.id)
        assertEquals("Special Product", product.title)
    }

    @Test
    fun getProductsReturnsServerErrorOn500() = runTest {
        val repository = DefaultProductRepository(createClient("{}", HttpStatusCode.InternalServerError))
        val result = repository.getProducts()

        assertIs<Result.Error<DataError.Network>>(result)
        assertEquals(DataError.Network.SERVER_ERROR, result.error)
    }
}
