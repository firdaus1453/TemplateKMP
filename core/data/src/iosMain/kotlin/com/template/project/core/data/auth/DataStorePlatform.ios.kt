package com.template.project.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import platform.Foundation.NSHomeDirectory

fun createPlatformDataStore(): DataStore<Preferences> {
    return createDataStore(
        producePath = {
            val dir = NSHomeDirectory() + "/Documents"
            "$dir/$DATA_STORE_FILE_NAME"
        },
    )
}
