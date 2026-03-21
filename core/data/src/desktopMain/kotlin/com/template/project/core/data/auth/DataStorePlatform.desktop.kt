package com.template.project.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File

fun createPlatformDataStore(): DataStore<Preferences> {
    return createDataStore(
        producePath = {
            val appDir = File(System.getProperty("user.home"), ".template-kmp")
            if (!appDir.exists()) appDir.mkdirs()
            File(appDir, DATA_STORE_FILE_NAME).absolutePath
        },
    )
}
