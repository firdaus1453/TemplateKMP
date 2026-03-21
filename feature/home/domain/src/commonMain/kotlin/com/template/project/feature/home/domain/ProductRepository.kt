package com.template.project.feature.home.domain

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.home.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(limit: Int = 20, skip: Int = 0): Result<List<Product>, DataError.Network>
    suspend fun getProductById(id: Int): Result<Product, DataError.Network>
}
