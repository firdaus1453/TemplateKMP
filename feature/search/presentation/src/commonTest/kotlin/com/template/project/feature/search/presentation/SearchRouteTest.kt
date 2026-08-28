package com.template.project.feature.search.presentation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchRouteTest {

    @Test
    fun searchRouteSerializationWorks() {
        val serialized = Json.encodeToString(SearchRoute)
        val deserialized = Json.decodeFromString<SearchRoute>(serialized)
        assertEquals(SearchRoute, deserialized)
    }
}
