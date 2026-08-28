package com.template.project.feature.profile.presentation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileRouteTest {

    @Test
    fun profileRouteSerializationWorks() {
        val serialized = Json.encodeToString(ProfileRoute)
        val deserialized = Json.decodeFromString<ProfileRoute>(serialized)
        assertEquals(ProfileRoute, deserialized)
    }
}
