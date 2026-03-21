package com.template.project.feature.notifications.domain

interface NotificationManager {
    fun requestPermission()
    fun isPermissionGranted(): Boolean
}

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
)
