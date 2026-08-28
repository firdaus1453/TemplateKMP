package com.template.project.feature.settings.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.template.project.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAppPreferencesTest {

    private fun createTestDataStore(): DefaultAppPreferences {
        val randomName = "test_settings_${Random.nextInt(100000)}.preferences_pb"
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { randomName.toPath() }
        )
        return DefaultAppPreferences(dataStore)
    }

    @Test
    fun defaultThemeModeIsSystem() = runTest {
        val prefs = createTestDataStore()
        val theme = prefs.observeThemeMode().first()
        assertEquals(ThemeMode.SYSTEM, theme)
    }

    @Test
    fun setThemeModeLightUpdatesFlow() = runTest {
        val prefs = createTestDataStore()
        prefs.setThemeMode(ThemeMode.LIGHT)
        val theme = prefs.observeThemeMode().first()
        assertEquals(ThemeMode.LIGHT, theme)
    }

    @Test
    fun setThemeModeDarkUpdatesFlow() = runTest {
        val prefs = createTestDataStore()
        prefs.setThemeMode(ThemeMode.DARK)
        val theme = prefs.observeThemeMode().first()
        assertEquals(ThemeMode.DARK, theme)
    }

    @Test
    fun setThemeModeSystemUpdatesFlow() = runTest {
        val prefs = createTestDataStore()
        prefs.setThemeMode(ThemeMode.DARK)
        prefs.setThemeMode(ThemeMode.SYSTEM)
        val theme = prefs.observeThemeMode().first()
        assertEquals(ThemeMode.SYSTEM, theme)
    }
}
