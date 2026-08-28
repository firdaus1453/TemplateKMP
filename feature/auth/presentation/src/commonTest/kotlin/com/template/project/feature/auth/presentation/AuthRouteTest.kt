package com.template.project.feature.auth.presentation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRouteTest {

    @Test
    fun loginRouteSerializationWorks() {
        val serialized = Json.encodeToString(LoginRoute)
        val deserialized = Json.decodeFromString<LoginRoute>(serialized)
        assertEquals(LoginRoute, deserialized)
    }

    @Test
    fun registerRouteSerializationWorks() {
        val serialized = Json.encodeToString(RegisterRoute)
        val deserialized = Json.decodeFromString<RegisterRoute>(serialized)
        assertEquals(RegisterRoute, deserialized)
    }
}
