package com.template.project.feature.settings.data.di

import com.template.project.feature.settings.data.DefaultAppPreferences
import com.template.project.feature.settings.domain.AppPreferences
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsDataModule = module {
    singleOf(::DefaultAppPreferences).bind<AppPreferences>()
}
