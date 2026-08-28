package com.template.project.feature.notifications.presentation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationsRouteTest {

    @Test
    fun notificationsRouteSerializationWorks() {
        val serialized = Json.encodeToString(NotificationsRoute)
        val deserialized = Json.decodeFromString<NotificationsRoute>(serialized)
        assertEquals(NotificationsRoute, deserialized)
    }
}
