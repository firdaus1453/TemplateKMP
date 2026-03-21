package com.template.project.feature.home.presentation

import com.template.project.feature.home.domain.model.Product

data class HomeState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface HomeAction {
    data object OnRefresh : HomeAction
    data class OnProductClick(val productId: Int) : HomeAction
}

sealed interface HomeEvent {
    data class NavigateToDetail(val productId: Int) : HomeEvent
}
