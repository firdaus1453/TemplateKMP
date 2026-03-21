package com.template.project.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.project.core.domain.result.Result
import com.template.project.feature.home.domain.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state
        .onStart { loadProducts() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HomeState(),
        )

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnRefresh -> loadProducts()
            is HomeAction.OnProductClick -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToDetail(action.productId))
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = productRepository.getProducts()) {
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                }
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            products = result.data,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }
}
