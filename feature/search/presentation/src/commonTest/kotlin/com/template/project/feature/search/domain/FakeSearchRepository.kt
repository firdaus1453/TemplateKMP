package com.template.project.feature.search.domain

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result

class FakeSearchRepository : SearchRepository {

    var searchResult: Result<List<SearchResult>, DataError.Network> =
        Result.Success(emptyList())

    var searchCallCount = 0
        private set

    var lastQuery: String? = null
        private set

    override suspend fun search(query: String): Result<List<SearchResult>, DataError.Network> {
        searchCallCount++
        lastQuery = query
        return searchResult
    }
}
