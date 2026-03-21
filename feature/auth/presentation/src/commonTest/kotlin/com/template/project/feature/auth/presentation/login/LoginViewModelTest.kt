package com.template.project.feature.auth.presentation.login

import app.cash.turbine.test
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.auth.domain.FakeAuthRepository
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
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = LoginViewModel(fakeAuthRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasEmptyFields() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            assertEquals("", initial.username)
            assertEquals("", initial.password)
            assertFalse(initial.isLoading)
            assertFalse(initial.canLogin)
        }
    }

    @Test
    fun onUsernameChangedUpdatesState() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onAction(LoginAction.OnUsernameChanged("emilys"))
            val updated = awaitItem()
            assertEquals("emilys", updated.username)
        }
    }

    @Test
    fun canLoginIsTrueWhenBothFieldsFilled() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onAction(LoginAction.OnUsernameChanged("emilys"))
            awaitItem()

            viewModel.onAction(LoginAction.OnPasswordChanged("emilyspass"))
            val updated = awaitItem()
            assertTrue(updated.canLogin)
        }
    }

    @Test
    fun canLoginIsFalseWhenUsernameEmpty() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onAction(LoginAction.OnPasswordChanged("emilyspass"))
            val updated = awaitItem()
            assertFalse(updated.canLogin)
        }
    }

    @Test
    fun togglePasswordVisibilityFlipsFlag() = runTest {
        viewModel.state.test {
            awaitItem() // initial

            viewModel.onAction(LoginAction.OnTogglePasswordVisibility)
            val toggled = awaitItem()
            assertTrue(toggled.isPasswordVisible)

            viewModel.onAction(LoginAction.OnTogglePasswordVisibility)
            val toggledBack = awaitItem()
            assertFalse(toggledBack.isPasswordVisible)
        }
    }

    @Test
    fun loginSuccessEmitsLoginSuccessEvent() = runTest {
        fakeAuthRepository.loginResult = Result.Success(Unit)

        viewModel.events.test {
            viewModel.onAction(LoginAction.OnUsernameChanged("emilys"))
            viewModel.onAction(LoginAction.OnPasswordChanged("emilyspass"))
            viewModel.onAction(LoginAction.OnLoginClick)

            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertIs<LoginEvent.LoginSuccess>(event)
        }
    }

    @Test
    fun loginErrorEmitsShowErrorEvent() = runTest {
        fakeAuthRepository.loginResult =
            Result.Error(DataError.Network.UNAUTHORIZED)

        viewModel.events.test {
            viewModel.onAction(LoginAction.OnUsernameChanged("wrong"))
            viewModel.onAction(LoginAction.OnPasswordChanged("wrong"))
            viewModel.onAction(LoginAction.OnLoginClick)

            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertIs<LoginEvent.ShowError>(event)
        }
    }

    @Test
    fun loginCallsRepositoryOnce() = runTest {
        viewModel.onAction(LoginAction.OnLoginClick)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, fakeAuthRepository.loginCallCount)
    }
}
