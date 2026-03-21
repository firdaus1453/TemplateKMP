package com.template.project.core.data.di

import com.template.project.core.data.auth.DataStoreSessionStorage
import com.template.project.core.data.auth.DefaultAuthRepository
import com.template.project.core.data.networking.HttpClientFactory
import com.template.project.core.data.networking.createPlatformEngine
import com.template.project.core.domain.auth.LogoutHandler
import com.template.project.core.domain.auth.SessionStorage
import com.template.project.feature.auth.domain.AuthRepository
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    // DataStore<Preferences> is provided by
    // platformDataStoreModule (platform-specific DI)

    singleOf(::DataStoreSessionStorage).bind<SessionStorage>()

    single<HttpClientFactory> {
        HttpClientFactory(sessionStorage = get())
    }

    single<HttpClient> {
        get<HttpClientFactory>().create(createPlatformEngine())
    }

    single { DefaultAuthRepository(get(), get()) }
    single<AuthRepository> { get<DefaultAuthRepository>() }
    single<LogoutHandler> { get<DefaultAuthRepository>() }
}

