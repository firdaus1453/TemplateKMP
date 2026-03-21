package com.template.project.feature.search.data

import com.template.project.core.data.networking.safeGet
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.core.domain.result.map
import com.template.project.feature.search.domain.SearchRepository
import com.template.project.feature.search.domain.SearchResult
import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable

@Serializable
private data class ProductSearchDto(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val price: Double,
)

@Serializable
private data class SearchResponse(
    val products: List<ProductSearchDto>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

class DefaultSearchRepository(
    private val httpClient: HttpClient,
) : SearchRepository {

    override suspend fun search(query: String): Result<List<SearchResult>, DataError.Network> {
        return httpClient.safeGet<SearchResponse>(
            route = "/products/search",
            queryParams = mapOf("q" to query),
        ).map { response ->
            response.products.map { dto ->
                SearchResult(
                    id = dto.id,
                    title = dto.title,
                    description = dto.description,
                    thumbnail = dto.thumbnail,
                    category = dto.category,
                    price = dto.price,
                )
            }
        }
    }
}
