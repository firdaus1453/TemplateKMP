package com.template.project.feature.settings.presentation

import app.cash.turbine.test
import com.template.project.feature.settings.domain.FakeAppPreferences
import com.template.project.feature.settings.domain.model.ThemeMode
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var fakeAppPreferences: FakeAppPreferences
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAppPreferences = FakeAppPreferences()
        viewModel = SettingsViewModel(fakeAppPreferences)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasSystemTheme() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            assertEquals(ThemeMode.SYSTEM, initial.themeMode)
        }
    }

    @Test
    fun changingThemeModeUpdatesPreferences() = runTest {
        viewModel.onAction(SettingsAction.OnThemeModeChanged(ThemeMode.DARK))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, fakeAppPreferences.setThemeCallCount)
    }

    @Test
    fun themeChangeReflectsInState() = runTest {
        viewModel.state.test {
            awaitItem() // initial (SYSTEM)

            // Trigger observe by advancing
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onAction(SettingsAction.OnThemeModeChanged(ThemeMode.DARK))
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertEquals(ThemeMode.DARK, updated.themeMode)
        }
    }

    @Test
    fun changingToLightModeWorks() = runTest {
        viewModel.state.test {
            awaitItem() // initial
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onAction(SettingsAction.OnThemeModeChanged(ThemeMode.LIGHT))
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = expectMostRecentItem()
            assertEquals(ThemeMode.LIGHT, updated.themeMode)
        }
    }
}
