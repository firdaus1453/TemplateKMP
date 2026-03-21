package com.template.project.core.data.di

import com.template.project.core.data.auth.createPlatformDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformDataStoreModule = module {
    single {
        createPlatformDataStore(androidContext())
    }
}
