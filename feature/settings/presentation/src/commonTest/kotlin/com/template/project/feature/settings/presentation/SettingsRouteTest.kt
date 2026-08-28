package com.template.project.feature.settings.presentation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRouteTest {

    @Test
    fun settingsRouteSerializationWorks() {
        val serialized = Json.encodeToString(SettingsRoute)
        val deserialized = Json.decodeFromString<SettingsRoute>(serialized)
        assertEquals(SettingsRoute, deserialized)
    }
}
