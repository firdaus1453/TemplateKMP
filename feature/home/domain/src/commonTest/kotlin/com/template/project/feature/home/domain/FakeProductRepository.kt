package com.template.project.feature.home.domain

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.home.domain.model.Product

class FakeProductRepository : ProductRepository {

    var productsResult: Result<List<Product>, DataError.Network> =
        Result.Success(emptyList())

    var singleProductResult: Result<Product, DataError.Network> =
        Result.Error(DataError.Network.UNKNOWN)

    var fetchCallCount = 0
        private set

    override suspend fun getProducts(
        limit: Int,
        skip: Int,
    ): Result<List<Product>, DataError.Network> {
        fetchCallCount++
        return productsResult
    }

    override suspend fun getProductById(id: Int): Result<Product, DataError.Network> {
        return singleProductResult
    }
}
