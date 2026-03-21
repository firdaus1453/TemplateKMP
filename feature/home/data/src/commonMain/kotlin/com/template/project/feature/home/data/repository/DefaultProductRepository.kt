package com.template.project.feature.home.data.repository

import com.template.project.core.data.networking.safeGet
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.core.domain.result.map
import com.template.project.feature.home.data.dto.ProductDto
import com.template.project.feature.home.data.dto.ProductListResponse
import com.template.project.feature.home.data.mapper.toDomain
import com.template.project.feature.home.domain.ProductRepository
import com.template.project.feature.home.domain.model.Product
import io.ktor.client.HttpClient

class DefaultProductRepository(
    private val httpClient: HttpClient,
) : ProductRepository {

    override suspend fun getProducts(
        limit: Int,
        skip: Int,
    ): Result<List<Product>, DataError.Network> {
        return httpClient.safeGet<ProductListResponse>(
            route = "/products",
            queryParams = mapOf(
                "limit" to limit,
                "skip" to skip,
            ),
        ).map { response ->
            response.products.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(id: Int): Result<Product, DataError.Network> {
        return httpClient.safeGet<ProductDto>(
            route = "/products/$id",
        ).map { it.toDomain() }
    }
}
