package com.template.project.feature.media.presentation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaRouteTest {

    @Test
    fun mediaRouteSerializationWorks() {
        val serialized = Json.encodeToString(MediaRoute)
        val deserialized = Json.decodeFromString<MediaRoute>(serialized)
        assertEquals(MediaRoute, deserialized)
    }
}
