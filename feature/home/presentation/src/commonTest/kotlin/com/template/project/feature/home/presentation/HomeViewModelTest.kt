package com.template.project.feature.home.presentation

import app.cash.turbine.test
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.home.domain.FakeProductRepository
import com.template.project.feature.home.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeProductRepository: FakeProductRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testProducts = listOf(
        Product(
            id = 1,
            title = "iPhone 9",
            description = "An apple mobile phone",
            category = "smartphones",
            price = 549.0,
            discountPercentage = 12.96,
            rating = 4.69,
            stock = 94,
            brand = "Apple",
            thumbnail = "https://example.com/thumb.png",
            images = emptyList(),
        ),
        Product(
            id = 2,
            title = "iPhone X",
            description = "Apple flagship",
            category = "smartphones",
            price = 899.0,
            discountPercentage = 17.94,
            rating = 4.44,
            stock = 34,
            brand = "Apple",
            thumbnail = "https://example.com/thumb2.png",
            images = emptyList(),
        ),
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeProductRepository = FakeProductRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun successfulFetchUpdatesStateWithProducts() = runTest {
        fakeProductRepository.productsResult = Result.Success(testProducts)
        viewModel = HomeViewModel(fakeProductRepository)

        viewModel.state.test {
            val initial = awaitItem()
            assertTrue(initial.products.isEmpty())

            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.products.size)
            assertEquals("iPhone 9", loaded.products[0].title)
        }
    }

    @Test
    fun errorFetchKeepsEmptyProducts() = runTest {
        fakeProductRepository.productsResult =
            Result.Error(DataError.Network.SERVER_ERROR)
        viewModel = HomeViewModel(fakeProductRepository)

        viewModel.state.test {
            awaitItem() // initial

            testDispatcher.scheduler.advanceUntilIdle()

            val afterError = expectMostRecentItem()
            assertFalse(afterError.isLoading)
            assertTrue(afterError.products.isEmpty())
        }
    }

    @Test
    fun refreshTriggersNewFetch() = runTest {
        fakeProductRepository.productsResult = Result.Success(testProducts)
        viewModel = HomeViewModel(fakeProductRepository)

        // Subscribe to state to trigger onStart
        viewModel.state.test {
            awaitItem() // initial
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem() // after first load

            viewModel.onAction(HomeAction.OnRefresh)
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            assertEquals(2, fakeProductRepository.fetchCallCount)
        }
    }

    @Test
    fun onProductClickEmitsNavigateEvent() = runTest {
        viewModel = HomeViewModel(fakeProductRepository)

        viewModel.events.test {
            viewModel.onAction(HomeAction.OnProductClick(productId = 42))
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertIs<HomeEvent.NavigateToDetail>(event)
            assertEquals(42, event.productId)
        }
    }
}
