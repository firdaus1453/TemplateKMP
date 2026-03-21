package com.template.project.feature.profile.presentation

import app.cash.turbine.test
import com.template.project.core.domain.auth.FakeLogoutHandler
import com.template.project.core.domain.model.User
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.profile.domain.FakeProfileRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var fakeLogoutHandler: FakeLogoutHandler
    private val testDispatcher = StandardTestDispatcher()

    private val testUser = User(
        id = 1,
        username = "emilys",
        email = "emily.johnson@x.dummyjson.com",
        firstName = "Emily",
        lastName = "Johnson",
        image = "https://example.com/avatar.png",
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeProfileRepository = FakeProfileRepository()
        fakeLogoutHandler = FakeLogoutHandler()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun successfulProfileFetchUpdatesState() = runTest {
        fakeProfileRepository.currentUserResult = Result.Success(testUser)
        viewModel = ProfileViewModel(fakeProfileRepository, fakeLogoutHandler)

        viewModel.state.test {
            awaitItem() // initial

            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertNotNull(loaded.user)
            assertEquals("emilys", loaded.user!!.username)
        }
    }

    @Test
    fun failedProfileFetchEmitsError() = runTest {
        fakeProfileRepository.currentUserResult =
            Result.Error(DataError.Network.UNAUTHORIZED)
        viewModel = ProfileViewModel(fakeProfileRepository, fakeLogoutHandler)

        // Subscribe to state to trigger onStart which calls loadProfile()
        viewModel.state.test {
            awaitItem() // initial
            testDispatcher.scheduler.advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        // Check events
        viewModel.events.test {
            // Re-trigger to get event
            viewModel.onAction(ProfileAction.OnRefresh)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertIs<ProfileEvent.ShowError>(event)
        }
    }

    @Test
    fun logoutCallsRepositoryAndEmitsEvent() = runTest {
        viewModel = ProfileViewModel(fakeProfileRepository, fakeLogoutHandler)

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnLogoutClick)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertIs<ProfileEvent.LogoutSuccess>(event)
            assertTrue(fakeLogoutHandler.logoutCalled)
        }
    }

    @Test
    fun refreshTriggersNewProfileFetch() = runTest {
        fakeProfileRepository.currentUserResult = Result.Success(testUser)
        viewModel = ProfileViewModel(fakeProfileRepository, fakeLogoutHandler)

        // Subscribe to state to trigger onStart
        viewModel.state.test {
            awaitItem() // initial
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem() // after first load

            viewModel.onAction(ProfileAction.OnRefresh)
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            assertEquals(2, fakeProfileRepository.fetchCallCount)
        }
    }
}

