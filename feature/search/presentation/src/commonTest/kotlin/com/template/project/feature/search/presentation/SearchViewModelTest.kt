package com.template.project.feature.search.presentation

import app.cash.turbine.test
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.search.domain.FakeSearchRepository
import com.template.project.feature.search.domain.SearchResult
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private lateinit var fakeSearchRepository: FakeSearchRepository
    private val testDispatcher = StandardTestDispatcher()

    private val testResults = listOf(
        SearchResult(id = 1, title = "iPhone 9", description = "Phone", thumbnail = "", category = "smartphones", price = 549.0),
        SearchResult(id = 2, title = "iPhone X", description = "Phone", thumbnail = "", category = "smartphones", price = 899.0),
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeSearchRepository = FakeSearchRepository()
        viewModel = SearchViewModel(fakeSearchRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsEmpty() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            assertEquals("", initial.query)
            assertTrue(initial.results.isEmpty())
            assertFalse(initial.isLoading)
        }
    }

    @Test
    fun queryChangeUpdatesState() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onAction(SearchAction.OnQueryChanged("phone"))
            val updated = awaitItem()
            assertEquals("phone", updated.query)
        }
    }

    @Test
    fun searchReturnsResultsAfterDebounce() = runTest {
        fakeSearchRepository.searchResult = Result.Success(testResults)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onAction(SearchAction.OnQueryChanged("phone"))
            awaitItem() // query updated

            // Advance past debounce (300ms)
            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.runCurrent()

            // May get loading state first
            val finalState = expectMostRecentItem()
            assertEquals(2, finalState.results.size)
            assertFalse(finalState.isLoading)
        }
    }

    @Test
    fun clearQueryResetsState() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onAction(SearchAction.OnQueryChanged("phone"))
            awaitItem()

            viewModel.onAction(SearchAction.OnClearQuery)
            val cleared = awaitItem()
            assertEquals("", cleared.query)
            assertTrue(cleared.results.isEmpty())
        }
    }

    @Test
    fun blankQueryDoesNotCallRepository() = runTest {
        viewModel.onAction(SearchAction.OnQueryChanged("   "))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, fakeSearchRepository.searchCallCount)
    }

    @Test
    fun errorKeepsEmptyResults() = runTest {
        fakeSearchRepository.searchResult =
            Result.Error(DataError.Network.SERVER_ERROR)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onAction(SearchAction.OnQueryChanged("test"))
            awaitItem() // query updated

            testDispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state.results.isEmpty())
            assertFalse(state.isLoading)
        }
    }
}
