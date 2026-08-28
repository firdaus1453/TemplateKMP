---
name: kmp-di
description: >
  Koin 4.x Multiplatform dependency injection guidelines for KMP & CMP projects. Trigger when: creating or modifying Koin modules, registering ViewModels (viewModelOf), singletons (singleOf), injecting dependencies via koinViewModel() / koinInject(), or configuring startKoin initialization.
---

# Dependency Injection (Koin Multiplatform 4.x)

This document defines conventions for configuring Koin Multiplatform 4.x, module organization, ViewModel scoping, and composition root wiring.

---

## 📌 Principles of Koin in KMP

1. **One Koin Module per Feature**: Each feature defines its own DI module (e.g. `authModule`, `homeModule`, `settingsModule`).
2. **`singleOf` & `viewModelOf` Constructors**: Use constructor DSL (`singleOf(::DefaultProductRepository) { bind<ProductRepository>() }`) for zero boilerplate.
3. **Lazy Graph Resolution**: Koin resolves dependencies on demand without reflection.
4. **Inject ViewModels at Root Only**: In Compose, inject ViewModels only in top-level `{Screen}Root` composables using `koinViewModel()`.

---

## 🛠️ Feature DI Modules

### 1. Repository & UseCase Module (`feature/{name}/di/{Name}Module.kt`)
```kotlin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    // Data Sources
    singleOf(::RemoteProductDataSource)
    singleOf(::LocalProductDataSource)

    // Repository bound to Domain Interface
    singleOf(::DefaultProductRepository) { bind<ProductRepository>() }

    // ViewModel
    viewModelOf(::HomeViewModel)
}
```

### 2. Core Modules (`core/data/di/CoreDataModule.kt`)
```kotlin
val coreDataModule = module {
    single { HttpClientFactory(get()).build(get()) }
    singleOf(::DataStoreSessionStorage) { bind<SessionStorage>() }
}
```

---

## 🚀 App Initialization (`composeApp/.../initKoin.kt`)

In `:composeApp`, aggregate all modules and launch Koin:

```kotlin
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            coreDataModule,
            corePresentationModule,
            authModule,
            homeModule,
            searchModule,
            profileModule,
            settingsModule,
        )
    }
}
```

---

## 🧠 ViewModel & Composable Injection

```kotlin
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = koinViewModel(), // Automatically scoped to NavBackStackEntry / ViewModelStore
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state = state, onAction = viewModel::onAction)
}
```
