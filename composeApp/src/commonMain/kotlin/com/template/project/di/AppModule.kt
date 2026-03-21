package com.template.project.di

import com.template.project.core.data.di.coreDataModule
import com.template.project.feature.auth.presentation.di.authPresentationModule
import com.template.project.feature.home.data.di.homeDataModule
import com.template.project.feature.home.presentation.di.homePresentationModule
import com.template.project.feature.profile.data.di.profileDataModule
import com.template.project.feature.profile.presentation.di.profilePresentationModule
import com.template.project.feature.search.data.di.searchDataModule
import com.template.project.feature.search.presentation.di.searchPresentationModule
import com.template.project.feature.settings.data.di.settingsDataModule
import com.template.project.feature.settings.presentation.di.settingsPresentationModule
import org.koin.dsl.module

val appModule = module {
    includes(
        // Core
        coreDataModule,

        // Auth
        authPresentationModule,

        // Home
        homeDataModule,
        homePresentationModule,

        // Profile
        profileDataModule,
        profilePresentationModule,

        // Search
        searchDataModule,
        searchPresentationModule,

        // Settings
        settingsDataModule,
        settingsPresentationModule,
    )
}
