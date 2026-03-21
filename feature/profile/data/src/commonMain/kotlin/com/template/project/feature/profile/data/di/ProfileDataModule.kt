package com.template.project.feature.profile.data.di

import com.template.project.feature.profile.data.repository.DefaultProfileRepository
import com.template.project.feature.profile.domain.ProfileRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val profileDataModule = module {
    singleOf(::DefaultProfileRepository).bind<ProfileRepository>()
}
