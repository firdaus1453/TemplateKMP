package com.template.project.feature.search.data.di

import com.template.project.feature.search.data.DefaultSearchRepository
import com.template.project.feature.search.domain.SearchRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val searchDataModule = module {
    singleOf(::DefaultSearchRepository).bind<SearchRepository>()
}
