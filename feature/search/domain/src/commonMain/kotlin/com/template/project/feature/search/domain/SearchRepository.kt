package com.template.project.feature.search.domain

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result

interface SearchRepository {
    suspend fun search(query: String): Result<List<SearchResult>, DataError.Network>
}

data class SearchResult(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val price: Double,
)
