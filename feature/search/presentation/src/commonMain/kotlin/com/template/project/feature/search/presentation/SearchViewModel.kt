package com.template.project.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.project.core.domain.result.Result
import com.template.project.feature.search.domain.SearchRepository
import com.template.project.feature.search.domain.SearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface SearchAction {
    data class OnQueryChanged(val query: String) : SearchAction
    data object OnClearQuery : SearchAction
}

class SearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state = _state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchState())

    private var searchJob: Job? = null

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnQueryChanged -> {
                _state.update { it.copy(query = action.query) }
                searchDebounced(action.query)
            }
            SearchAction.OnClearQuery -> {
                _state.update { SearchState() }
                searchJob?.cancel()
            }
        }
    }

    private fun searchDebounced(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _state.update { it.copy(isLoading = true) }
            when (val result = searchRepository.search(query)) {
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                }
                is Result.Success -> {
                    _state.update { it.copy(results = result.data, isLoading = false) }
                }
            }
        }
    }
}
