package com.template.project.feature.notifications.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationManagerTest {

    private class FakeNotificationManager : NotificationManager {
        var permissionGranted = false
        var requestPermissionCalled = false

        override fun requestPermission() {
            requestPermissionCalled = true
        }

        override fun isPermissionGranted(): Boolean = permissionGranted
    }

    @Test
    fun notificationItemPropertiesAreCorrect() {
        val item = NotificationItem(
            id = "notif_1",
            title = "Order Confirmed",
            body = "Your order #1234 has been confirmed.",
            timestamp = 1700000000000L,
            isRead = true
        )

        assertEquals("notif_1", item.id)
        assertEquals("Order Confirmed", item.title)
        assertEquals("Your order #1234 has been confirmed.", item.body)
        assertEquals(1700000000000L, item.timestamp)
        assertTrue(item.isRead)
    }

    @Test
    fun notificationManagerRequestPermissionSetsFlag() {
        val manager = FakeNotificationManager()
        assertFalse(manager.isPermissionGranted())
        manager.requestPermission()
        assertTrue(manager.requestPermissionCalled)
    }
}
