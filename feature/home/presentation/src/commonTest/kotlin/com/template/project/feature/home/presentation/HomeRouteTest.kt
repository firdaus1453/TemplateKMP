package com.template.project.feature.home.presentation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeRouteTest {

    @Test
    fun homeRouteSerializationWorks() {
        val serialized = Json.encodeToString(HomeRoute)
        val deserialized = Json.decodeFromString<HomeRoute>(serialized)
        assertEquals(HomeRoute, deserialized)
    }
}
