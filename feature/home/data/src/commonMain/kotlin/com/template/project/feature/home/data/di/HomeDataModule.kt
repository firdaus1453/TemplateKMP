package com.template.project.feature.home.data.di

import com.template.project.feature.home.data.repository.DefaultProductRepository
import com.template.project.feature.home.domain.ProductRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val homeDataModule = module {
    singleOf(::DefaultProductRepository).bind<ProductRepository>()
}
