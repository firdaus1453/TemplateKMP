package com.template.project.feature.settings.domain

import com.template.project.feature.settings.domain.model.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeModeTest {

    @Test
    fun themeModeEntriesContainAllModes() {
        val entries = ThemeMode.entries
        assertEquals(3, entries.size)
        assertTrue(entries.contains(ThemeMode.SYSTEM))
        assertTrue(entries.contains(ThemeMode.LIGHT))
        assertTrue(entries.contains(ThemeMode.DARK))
    }
}
