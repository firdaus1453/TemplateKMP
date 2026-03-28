<div align="center">

# 📖 TemplateKMP — Developer Guide

**Panduan lengkap untuk junior developer: dari setup sampai production**

</div>

---

## 📑 Daftar Isi

1. [Persiapan Lingkungan (Environment Setup)](#1-persiapan-lingkungan-environment-setup)
2. [Clone & Menjalankan Project](#2-clone--menjalankan-project)
3. [Memahami Struktur Project](#3-memahami-struktur-project)
4. [Memahami Convention Plugins](#4-memahami-convention-plugins)
5. [Memahami Core Modules](#5-memahami-core-modules)
6. [Memahami Feature Modules](#6-memahami-feature-modules)
7. [Pattern MVI (Model-View-Intent)](#7-pattern-mvi-model-view-intent)
8. [Dependency Injection dengan Koin](#8-dependency-injection-dengan-koin)
9. [Navigation](#9-navigation)
10. [Networking dengan Ktor](#10-networking-dengan-ktor)
11. [Tutorial: Membuat Feature Module Baru](#11-tutorial-membuat-feature-module-baru)
12. [Testing](#12-testing)
13. [Konfigurasi BuildKonfig & Secrets](#13-konfigurasi-buildkonfig--secrets)
14. [Menjalankan di Setiap Platform](#14-menjalankan-di-setiap-platform)
15. [Menuju Production](#15-menuju-production)
16. [Troubleshooting](#16-troubleshooting)
17. [Konvensi & Best Practices](#17-konvensi--best-practices)

---

## 1. Persiapan Lingkungan (Environment Setup)

### 1.1 Software yang Dibutuhkan

| Software | Versi Minimum | Keterangan |
|----------|---------------|------------|
| **Android Studio** | Ladybug (2024.2+) | IDE utama, sudah termasuk KMP plugin |
| **AGP** | 9.1+ | Base config & KMP conventions |
| **JDK** | 17+ | Wajib untuk Gradle dan kompilasi Kotlin |
| **Xcode** | 15+ | Hanya untuk macOS, diperlukan untuk build iOS |
| **Xcode Command Line Tools** | - | `xcode-select --install` |
| **CocoaPods** (opsional) | - | Jika ada dependensi iOS native |
| **Git** | - | Version control |

### 1.2 Instalasi Step-by-Step

**Android Studio:**
1. Download dari [developer.android.com](https://developer.android.com/studio)
2. Install dan buka Android Studio
3. Pastikan **Kotlin Multiplatform** plugin sudah ter-install:
   - `Settings` → `Plugins` → cari "Kotlin Multiplatform" → Install
4. Install Android SDK (API 36) via `Settings` → `SDK Manager`

**JDK 17:**
```bash
# macOS (via Homebrew)
brew install openjdk@17

# Verifikasi
java -version
# Output: openjdk version "17.x.x"
```

**Xcode (macOS only):**
```bash
# Install Command Line Tools
xcode-select --install

# Buka Xcode minimal 1x untuk accept license
open -a Xcode

# Verifikasi
xcodebuild -version
```

### 1.3 Verifikasi Environment

Jalankan checklist ini sebelum memulai:

```bash
# ✅ JDK 17+
java -version

# ✅ Kotlin
kotlin -version

# ✅ Android SDK (buka Android Studio → SDK Manager → pastikan API 36 terinstall)

# ✅ Xcode (macOS only)
xcodebuild -version

# ✅ Git
git --version
```

---

## 2. Clone & Menjalankan Project

### 2.1 Clone Repository

```bash
git clone https://github.com/firdaus1453/TemplateKMP.git
cd TemplateKMP
```

### 2.2 Setup `local.properties`

File ini berisi konfigurasi lokal yang **tidak boleh di-commit** ke Git.

```bash
# Copy dari contoh
cp local.properties.example local.properties
```

Buka `local.properties` dan sesuaikan:

```properties
# Path ke Android SDK (biasanya auto-detect oleh Android Studio)
sdk.dir=/Users/NAMA_KAMU/Library/Android/sdk

# API Configuration
API_BASE_URL=https://dummyjson.com
```

> ⚠️ **PENTING:** File `local.properties` sudah ada di `.gitignore`. **Jangan pernah** commit file ini karena berisi konfigurasi dan secrets lokal.

### 2.3 Sync & Build

```bash
# Buka project di Android Studio, lalu klik "Sync Now" saat muncul notifikasi

# Atau via terminal:
./gradlew assemble
```

> 💡 **Tips:** Build pertama kali akan memakan waktu cukup lama (~5-15 menit) karena Gradle perlu download semua dependency.

### 2.4 Menjalankan Aplikasi

```bash
# Android (pastikan emulator/device sudah tersambung)
./gradlew :composeApp:assembleDebug
# Atau klik tombol ▶ Run di Android Studio

# Desktop (JVM)
./gradlew :composeApp:run

# iOS (lihat detailnya di Section 14)
```

---

## 3. Memahami Struktur Project

### 3.1 Gambaran Besar

```
TemplateKMP/
├── 🔧 build-logic/convention/    ← Gradle convention plugins (aturan build yang reusable)
├── 📱 composeApp/                 ← Composition root (entry point semua platform)
├── 🏗️ core/
│   ├── domain/                    ← Pure Kotlin: Result type, Error model, interfaces
│   ├── data/                      ← Implementasi: Ktor client, DataStore, session
│   ├── presentation/              ← Shared UI utility: UiText, ObserveAsEvents
│   └── designsystem/             ← Theme, colors, typography, reusable UI components
├── 🧩 feature/
│   ├── auth/        (domain, presentation)
│   ├── home/        (domain, data, presentation)
│   ├── profile/     (domain, data, presentation)
│   ├── settings/    (domain, data, presentation)
│   ├── search/      (domain, data, presentation)
│   ├── notifications/ (domain, presentation)  ← stub
│   └── media/       (domain, presentation)    ← stub
├── gradle/libs.versions.toml      ← Version catalog (semua versi terpusat)
└── settings.gradle.kts            ← Daftar semua module
```

### 3.2 Apa itu "Composition Root"?

`composeApp` adalah **composition root** — tempat semua modul di-"wire" (disambungkan) menjadi satu aplikasi. Di sini terjadi:

- ✅ Inisialisasi **Koin** (dependency injection)
- ✅ Setup **NavHost** (navigasi antar layar)
- ✅ Entry point per platform (Android `MainActivity`, iOS `MainViewController`, Desktop `main()`)
- ✅ Import semua feature module

### 3.3 Dependency Rules (Aturan Ketergantungan)

```
composeApp → core/* + feature/*/
feature/*/presentation → feature/*/domain + core/presentation + core/designsystem
feature/*/data → feature/*/domain + core/domain + core/data
core/data → core/domain
core/presentation → core/domain + core/designsystem
```

**Dalam bahasa sederhana:**

| Layer | Boleh akses | TIDAK boleh akses |
|-------|------------|-------------------|
| `domain` | Tidak ada (pure Kotlin) | Framework apapun (Ktor, Compose, Room, dll) |
| `data` | `domain` miliknya + `core/domain` + `core/data` | `presentation`, feature lain |
| `presentation` | `domain` miliknya + `core/presentation` + `core/designsystem` | `data` layer, feature lain |
| `composeApp` | Semua module | - |

> 🔑 **Prinsip Utama:**
> - Feature A **tidak boleh** depend ke Feature B
> - `presentation` **tidak boleh** depend ke `data` (hanya ke `domain`)
> - `domain` harus **pure Kotlin** — tidak boleh import framework apapun

---

## 4. Memahami Convention Plugins

### 4.1 Apa itu Convention Plugin?

Convention plugin adalah Gradle plugin buatan sendiri yang **mengenkapsulasi konfigurasi build yang berulang**. Daripada menulis konfigurasi Kotlin Multiplatform, Android, Compose, dll di setiap `build.gradle.kts`, kita cukup apply satu plugin.

### 4.2 Lokasi

```
build-logic/
└── convention/
    └── src/main/kotlin/
        ├── KmpLibraryConventionPlugin.kt
        ├── CmpLibraryConventionPlugin.kt
        ├── CmpFeatureConventionPlugin.kt
        ├── CmpApplicationConventionPlugin.kt
        ├── AndroidApplicationConventionPlugin.kt
        ├── AndroidApplicationComposeConventionPlugin.kt
        ├── KoverConventionPlugin.kt
        ├── RoomConventionPlugin.kt
        ├── BuildKonfigConventionPlugin.kt
        └── com/template/convention/
            ├── TargetConfigurations.kt    ← Android/iOS/Desktop target setup
            ├── ConfigureKotlinAndroid.kt   ← Android-specific config
            ├── ConfigureKotlinMultiplatform.kt
            └── Extensions.kt
```

### 4.3 Kapan Pakai Plugin yang Mana?

| Mau buat modul... | Pakai plugin | Contoh |
|-------------------|-------------|--------|
| Pure Kotlin (tanpa UI) | `template.kmp.library` | `feature/home/domain`, `core/domain` |
| Compose UI tanpa ViewModel | `template.cmp.library` | `core/designsystem`, `core/presentation` |
| Feature screen (Compose + ViewModel + Koin) | `template.cmp.feature` | `feature/home/presentation` |
| Main app module | `template.cmp.application` | `composeApp` |
| Butuh database Room | `template.room` | (tambahkan di modul yang perlu) |
| Butuh build constants | `template.buildkonfig` | `core/data` |
| Butuh code coverage | `template.kover` | (wajib ada tes minimal 1/dummy test, cegah "No tests discovered") |

### 4.4 Contoh Penggunaan

```kotlin
// feature/home/domain/build.gradle.kts — Pure Kotlin domain
plugins {
    alias(libs.plugins.convention.kmp.library)   // KMP tanpa Compose
    alias(libs.plugins.convention.kover)          // Test coverage
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.domain)  // Keyword: api = expose ke consumer
        }
    }
}
```

```kotlin
// feature/home/presentation/build.gradle.kts — Feature screen
plugins {
    alias(libs.plugins.convention.cmp.feature)   // Compose + ViewModel + Koin
    alias(libs.plugins.convention.kover)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.home.domain)  // Keyword: implementation = internal
            implementation(libs.bundles.coil)
        }
    }
}
```

> 💡 **`api` vs `implementation`:**
> - `api` = dependency ini **ter-expose** ke module yang depend pada kita
> - `implementation` = dependency ini **tersembunyi** (internal)
> - **Gunakan `api`** untuk `core/domain` di feature domain (agar presentation bisa akses model dari core)
> - **Gunakan `implementation`** untuk semua yang lain

---

## 5. Memahami Core Modules

### 5.1 `core/domain` — Fondasi Pure Kotlin

Berisi tipe-tipe dasar yang digunakan di **seluruh aplikasi**. Tidak boleh ada import framework apapun.

**`Result<D, E>`** — Sealed interface untuk menangani success/error tanpa exception:

```kotlin
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.template.project.core.domain.result.Error>(
        val error: E
    ) : Result<Nothing, E>
}
```

**Cara pakai:**

```kotlin
// Di repository interface
interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>, DataError.Network>
}

// Di ViewModel
when (val result = repository.getProducts()) {
    is Result.Success -> // result.data berisi List<Product>
    is Result.Error   -> // result.error berisi DataError.Network
}
```

**`DataError`** — Enum untuk representasi error:

```kotlin
sealed interface DataError : Error {
    enum class Network : DataError {
        REQUEST_TIMEOUT, UNAUTHORIZED, CONFLICT,
        TOO_MANY_REQUESTS, NO_INTERNET, SERVER_ERROR,
        SERIALIZATION, UNKNOWN,
    }
    enum class Local : DataError {
        DISK_FULL, UNKNOWN,
    }
}
```

> 🔑 Template ini menggunakan **`Result<D, E>`** bukan Kotlin `Result<T>` bawaan. Ini memberikan type-safety lebih karena error juga punya tipe yang jelas.

### 5.2 `core/data` — Implementasi Infrastruktur

Berisi implementasi networking, session storage, dan utility.

**`HttpClientFactory`** — Membuat Ktor `HttpClient` yang sudah dikonfigurasi:
- Content negotiation (JSON)
- Request timeout (20 detik)
- Logging via Kermit
- Bearer auth dengan automatic token refresh
- Skip auth untuk endpoint login/register

**`SessionStorage`** — Menyimpan auth token menggunakan DataStore (multiplatform).

**`safeGet` / helper functions** — Wrapper ktor yang otomatis map response ke `Result<T, DataError.Network>`.

### 5.3 `core/presentation` — Shared UI Utilities

- **`UiText`** — Sealed interface untuk text yang bisa berupa string biasa atau string resource (untuk multi-bahasa)
- **`ObserveAsEvents`** — Composable helper untuk menangkap one-time event dari ViewModel (navigasi, snackbar, dll)

### 5.4 `core/designsystem` — Design System

- **`AppTheme`** — Material 3 theme (light/dark mode)
- **`LoadingIndicator`** dan UI components lain yang reusable
- Centralized colors, typography, spacing

---

## 6. Memahami Feature Modules

### 6.1 Anatomi Feature Module

Setiap feature dibagi menjadi 2-3 sub-module:

```
feature/home/
├── domain/           ← Model + Repository interface (PURE KOTLIN)
│   ├── build.gradle.kts
│   └── src/commonMain/kotlin/.../
│       ├── model/
│       │   └── Product.kt           ← Data class (domain model)
│       └── ProductRepository.kt     ← Interface (kontrak)
│
├── data/             ← Implementasi Repository (KTOR, ROOM)
│   ├── build.gradle.kts
│   └── src/commonMain/kotlin/.../
│       ├── dto/
│       │   └── ProductDto.kt        ← Data class untuk JSON response
│       ├── mapper/
│       │   └── ProductMapper.kt     ← Dto → Domain model mapping
│       ├── repository/
│       │   └── DefaultProductRepository.kt  ← Implementasi
│       └── di/
│           └── HomeDataModule.kt    ← Koin DI module
│
└── presentation/     ← ViewModel + UI Screen (COMPOSE)
    ├── build.gradle.kts
    └── src/
        ├── commonMain/kotlin/.../
        │   ├── HomeContract.kt      ← State, Action, Event
        │   ├── HomeViewModel.kt     ← Business logic
        │   ├── HomeScreen.kt        ← UI composables
        │   └── di/
        │       └── HomePresentationModule.kt  ← Koin DI module
        └── commonTest/kotlin/.../
            └── HomeViewModelTest.kt ← Unit tests
```

### 6.2 Alur Data dalam Feature

```
┌──────────────────────────────────────────────────────────────────┐
│                         PRESENTATION                              │
│  HomeScreen ──(Action)──→ HomeViewModel ──(State)──→ HomeScreen  │
│       │                        │                          ↑       │
│       │                   (calls)                    (observe)    │
└───────│────────────────────────│──────────────────────────│───────┘
        │                        │                          │
        │              ┌─────────↓──────────┐               │
        │              │      DOMAIN        │               │
        │              │  ProductRepository │               │
        │              │   (interface)      │               │
        │              └─────────↑──────────┘               │
        │                        │                          │
        │              ┌─────────↓──────────┐               │
        │              │       DATA         │               │
        │              │ DefaultProduct     │               │
        │              │   Repository       │               │
        │              │  (Ktor HTTP call)  │               │
        │              └────────────────────┘               │
        └───────────────────────────────────────────────────┘
```

1. **User** berinteraksi dengan `HomeScreen` → mengirim **Action** ke ViewModel
2. **ViewModel** memanggil **Repository** (domain interface)
3. **Repository implementation** (data) melakukan network call via Ktor
4. **Result** dikembalikan → ViewModel update **State**
5. **Screen** otomatis re-render karena observe State

---

## 7. Pattern MVI (Model-View-Intent)

### 7.1 Komponen MVI

Setiap feature presentation memiliki **Contract file** dengan 3 komponen:

```kotlin
// HomeContract.kt

// STATE — Representasi tampilan saat ini
data class HomeState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
)

// ACTION — Aksi dari user (Intent)
sealed interface HomeAction {
    data object OnRefresh : HomeAction
    data class OnProductClick(val productId: Int) : HomeAction
}

// EVENT — Efek samping satu kali (navigasi, snackbar, dll)
sealed interface HomeEvent {
    data class NavigateToDetail(val productId: Int) : HomeEvent
}
```

### 7.2 ViewModel

```kotlin
class HomeViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {

    // 🔹 State — MutableStateFlow + WhileSubscribed
    private val _state = MutableStateFlow(HomeState())
    val state = _state
        .onStart { loadProducts() }  // Auto-load saat pertama kali di-subscribe
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),  // Stop 5 detik setelah terakhir observe
            HomeState(),
        )

    // 🔹 Events — Channel untuk one-time events
    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    // 🔹 Action handler
    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnRefresh -> loadProducts()
            is HomeAction.OnProductClick -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToDetail(action.productId))
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = productRepository.getProducts()) {
                is Result.Error -> _state.update { it.copy(isLoading = false) }
                is Result.Success -> _state.update {
                    it.copy(products = result.data, isLoading = false)
                }
            }
        }
    }
}
```

### 7.3 Screen

Setiap screen memiliki **2 composable**:

```kotlin
// 1️⃣ ROOT — Menghubungkan ViewModel dengan Screen (smart composable)
@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = koinViewModel(),  // Inject dari Koin
    onProductClick: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HomeEvent.NavigateToDetail -> onProductClick(event.productId)
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

// 2️⃣ SCREEN — Pure UI, menerima state dan callback (dumb composable)
@Composable
private fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    // Pure UI rendering berdasarkan state
    LazyColumn { ... }
}
```

> 💡 **Kenapa dipisah?**
> - `Root` = smart, ada side effect (ViewModel, navigation)
> - `Screen` = dumb, pure UI → mudah di-preview dan di-test

---

## 8. Dependency Injection dengan Koin

### 8.1 Membuat DI Module

Setiap layer di setiap feature memiliki Koin module sendiri:

```kotlin
// feature/home/data/di/HomeDataModule.kt
val homeDataModule = module {
    // singleOf = singleton, bind<Interface> = register sebagai interface
    singleOf(::DefaultProductRepository).bind<ProductRepository>()
}

// feature/home/presentation/di/HomePresentationModule.kt
val homePresentationModule = module {
    viewModelOf(::HomeViewModel)  // Khusus ViewModel
}
```

### 8.2 Mendaftarkan ke AppModule

Semua DI module harus didaftarkan di `composeApp`:

```kotlin
// composeApp/.../di/AppModule.kt
val appModule = module {
    includes(
        // Core (HARUS di-load pertama!)
        coreDataModule,

        // Feature: Home
        homeDataModule,
        homePresentationModule,

        // Feature: Auth
        authPresentationModule,

        // Feature lainnya...
    )
}
```

### 8.3 Pattern Koin yang Digunakan

| Pattern | Penjelasan | Contoh |
|---------|-----------|--------|
| `singleOf(::Class).bind<Interface>()` | Buat singleton, register sebagai interface | Repository |
| `viewModelOf(::Class)` | Buat ViewModel (scoped ke lifecycle) | ViewModel |
| `single<Type> { ... }` | Manual singleton definition | HttpClient |
| `get()` | Ambil dependency yang sudah di-register | Di dalam definisi lain |

> ⚠️ **PENTING:** `coreDataModule` **HARUS** di-include sebelum feature modules, karena feature bergantung pada `HttpClient` yang didefinisikan di core.

---

## 9. Navigation

### 9.1 Setup Navigation

Navigation menggunakan **Jetbrains Navigation Compose** dengan `@Serializable` routes.

**Definisi Route:**

```kotlin
// feature/home/presentation/HomeRoute.kt
@Serializable
data object HomeRoute   // Tanpa parameter

// Contoh route dengan parameter:
@Serializable
data class ProductDetailRoute(val productId: Int)
```

**Definisi Navigation Graphs:**

```kotlin
// composeApp/.../navigation/NavigationGraphs.kt
@Serializable
data object MainGraph    // Nested graph: halaman setelah login

@Serializable
data object AuthGraph    // Nested graph: halaman login/register
```

### 9.2 NavHost di `App.kt`

```kotlin
@Composable
fun App() {
    KoinContext {
        AppTheme {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = AuthGraph,  // Mulai dari login
            ) {
                // Auth flow
                navigation<AuthGraph>(startDestination = LoginRoute) {
                    composable<LoginRoute> {
                        LoginScreenRoot(
                            onLoginSuccess = {
                                navController.navigate(MainGraph) {
                                    popUpTo(AuthGraph) { inclusive = true }
                                }
                            },
                        )
                    }
                }

                // Main flow (setelah login)
                navigation<MainGraph>(startDestination = HomeRoute) {
                    composable<HomeRoute> {
                        MainScaffold(navController = navController, currentRoute = HomeRoute) {
                            HomeScreenRoot(
                                onProductClick = { productId ->
                                    // navController.navigate(ProductDetailRoute(productId))
                                },
                            )
                        }
                    }
                    // ... destination lainnya
                }
            }
        }
    }
}
```

### 9.3 Bottom Navigation

Template ini sudah menyediakan bottom navigation bar dengan 5 tab:

| Tab | Route | Icon |
|-----|-------|------|
| Home | `HomeRoute` | 🏠 Home |
| Search | `SearchRoute` | 🔍 Search |
| Notifications | `NotificationsRoute` | 🔔 Notifications |
| Profile | `ProfileRoute` | 👤 Person |
| Settings | `SettingsRoute` | ⚙️ Settings |

---

## 10. Networking dengan Ktor

### 10.1 Arsitektur Networking

```
HttpClientFactory (core/data)
       │
       ├── ContentNegotiation (JSON serialization)
       ├── HttpTimeout (20s)
       ├── Logging (Kermit)
       ├── Auth (Bearer token + auto refresh)
       └── defaultRequest (Content-Type: JSON)
```

### 10.2 Membuat API Call

**1. Buat DTO (Data Transfer Object):**

```kotlin
// feature/home/data/dto/ProductDto.kt
@Serializable
data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val thumbnail: String,
)

@Serializable
data class ProductListResponse(
    val products: List<ProductDto>,
    val total: Int,
)
```

**2. Buat Mapper (DTO → Domain Model):**

```kotlin
// feature/home/data/mapper/ProductMapper.kt
fun ProductDto.toDomain() = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    price = price,
    thumbnail = thumbnail,
    // ... field lainnya
)
```

**3. Implementasi Repository:**

```kotlin
class DefaultProductRepository(
    private val httpClient: HttpClient,
) : ProductRepository {

    override suspend fun getProducts(
        limit: Int, skip: Int,
    ): Result<List<Product>, DataError.Network> {
        return httpClient.safeGet<ProductListResponse>(
            route = "/products",
            queryParams = mapOf("limit" to limit, "skip" to skip),
        ).map { response ->
            response.products.map { it.toDomain() }
        }
    }
}
```

> 💡 **`safeGet`** adalah extension function dari `core/data` yang otomatis:
> - Menambahkan base URL dari BuildKonfig
> - Menangkap exception dan map ke `DataError.Network`
> - Mengembalikan `Result<T, DataError.Network>`

---

## 11. Tutorial: Membuat Feature Module Baru

Berikut tutorial lengkap membuat feature **"Bookmark"** dari nol.

### Step 1: Daftarkan module di `settings.gradle.kts`

```kotlin
// settings.gradle.kts — tambahkan di bagian bawah
// Feature: Bookmark
include(":feature:bookmark:domain")
include(":feature:bookmark:data")
include(":feature:bookmark:presentation")
```

### Step 2: Buat folder dan `build.gradle.kts`

**Domain:**

```bash
mkdir -p feature/bookmark/domain/src/commonMain/kotlin/com/template/project/feature/bookmark/domain/model
```

```kotlin
// feature/bookmark/domain/build.gradle.kts
plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kover)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.domain)
        }
    }
}
```

**Data:**

```bash
mkdir -p feature/bookmark/data/src/commonMain/kotlin/com/template/project/feature/bookmark/data/{dto,mapper,repository,di}
```

```kotlin
// feature/bookmark/data/build.gradle.kts
plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kover)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.feature.bookmark.domain)

            implementation(libs.bundles.ktor.common)
            implementation(libs.koin.core)
        }
    }
}
```

**Presentation:**

```bash
mkdir -p feature/bookmark/presentation/src/commonMain/kotlin/com/template/project/feature/bookmark/presentation/di
mkdir -p feature/bookmark/presentation/src/commonTest/kotlin/com/template/project/feature/bookmark/presentation
```

```kotlin
// feature/bookmark/presentation/build.gradle.kts
plugins {
    alias(libs.plugins.convention.cmp.feature)
    alias(libs.plugins.convention.kover)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.bookmark.domain)
        }
    }
}
```

### Step 3: Buat Domain Layer

```kotlin
// feature/bookmark/domain/model/Bookmark.kt
package com.template.project.feature.bookmark.domain.model

data class Bookmark(
    val id: Int,
    val title: String,
    val url: String,
    val createdAt: String,
)
```

```kotlin
// feature/bookmark/domain/BookmarkRepository.kt
package com.template.project.feature.bookmark.domain

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.feature.bookmark.domain.model.Bookmark

interface BookmarkRepository {
    suspend fun getBookmarks(): Result<List<Bookmark>, DataError.Network>
    suspend fun addBookmark(bookmark: Bookmark): Result<Bookmark, DataError.Network>
    suspend fun deleteBookmark(id: Int): Result<Unit, DataError.Network>
}
```

### Step 4: Buat Data Layer

```kotlin
// feature/bookmark/data/dto/BookmarkDto.kt
package com.template.project.feature.bookmark.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BookmarkDto(
    val id: Int,
    val title: String,
    val url: String,
    val createdAt: String,
)
```

```kotlin
// feature/bookmark/data/mapper/BookmarkMapper.kt
package com.template.project.feature.bookmark.data.mapper

import com.template.project.feature.bookmark.data.dto.BookmarkDto
import com.template.project.feature.bookmark.domain.model.Bookmark

fun BookmarkDto.toDomain() = Bookmark(
    id = id,
    title = title,
    url = url,
    createdAt = createdAt,
)
```

```kotlin
// feature/bookmark/data/repository/DefaultBookmarkRepository.kt
package com.template.project.feature.bookmark.data.repository

import com.template.project.core.data.networking.safeGet
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.core.domain.result.map
import com.template.project.feature.bookmark.data.dto.BookmarkDto
import com.template.project.feature.bookmark.data.mapper.toDomain
import com.template.project.feature.bookmark.domain.BookmarkRepository
import com.template.project.feature.bookmark.domain.model.Bookmark
import io.ktor.client.HttpClient

class DefaultBookmarkRepository(
    private val httpClient: HttpClient,
) : BookmarkRepository {

    override suspend fun getBookmarks(): Result<List<Bookmark>, DataError.Network> {
        return httpClient.safeGet<List<BookmarkDto>>(
            route = "/bookmarks",
        ).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun addBookmark(bookmark: Bookmark): Result<Bookmark, DataError.Network> {
        // Implementasi POST
        TODO("Implement POST /bookmarks")
    }

    override suspend fun deleteBookmark(id: Int): Result<Unit, DataError.Network> {
        // Implementasi DELETE
        TODO("Implement DELETE /bookmarks/{id}")
    }
}
```

```kotlin
// feature/bookmark/data/di/BookmarkDataModule.kt
package com.template.project.feature.bookmark.data.di

import com.template.project.feature.bookmark.data.repository.DefaultBookmarkRepository
import com.template.project.feature.bookmark.domain.BookmarkRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val bookmarkDataModule = module {
    singleOf(::DefaultBookmarkRepository).bind<BookmarkRepository>()
}
```

### Step 5: Buat Presentation Layer

```kotlin
// feature/bookmark/presentation/BookmarkContract.kt
package com.template.project.feature.bookmark.presentation

import com.template.project.feature.bookmark.domain.model.Bookmark

data class BookmarkState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface BookmarkAction {
    data object OnRefresh : BookmarkAction
    data class OnDeleteClick(val bookmarkId: Int) : BookmarkAction
}

sealed interface BookmarkEvent {
    data object BookmarkDeleted : BookmarkEvent
    data class ShowError(val message: String) : BookmarkEvent
}
```

```kotlin
// feature/bookmark/presentation/BookmarkViewModel.kt
package com.template.project.feature.bookmark.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.project.core.domain.result.Result
import com.template.project.feature.bookmark.domain.BookmarkRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarkState())
    val state = _state
        .onStart { loadBookmarks() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BookmarkState(),
        )

    private val _events = Channel<BookmarkEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: BookmarkAction) {
        when (action) {
            BookmarkAction.OnRefresh -> loadBookmarks()
            is BookmarkAction.OnDeleteClick -> deleteBookmark(action.bookmarkId)
        }
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = bookmarkRepository.getBookmarks()) {
                is Result.Error -> _state.update { it.copy(isLoading = false) }
                is Result.Success -> _state.update {
                    it.copy(bookmarks = result.data, isLoading = false)
                }
            }
        }
    }

    private fun deleteBookmark(id: Int) {
        viewModelScope.launch {
            when (bookmarkRepository.deleteBookmark(id)) {
                is Result.Error -> _events.send(BookmarkEvent.ShowError("Gagal menghapus"))
                is Result.Success -> {
                    _events.send(BookmarkEvent.BookmarkDeleted)
                    loadBookmarks()  // Refresh list
                }
            }
        }
    }
}
```

```kotlin
// feature/bookmark/presentation/BookmarkRoute.kt
package com.template.project.feature.bookmark.presentation

import kotlinx.serialization.Serializable

@Serializable
data object BookmarkRoute
```

```kotlin
// feature/bookmark/presentation/BookmarkScreen.kt
package com.template.project.feature.bookmark.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.project.core.designsystem.components.LoadingIndicator
import com.template.project.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookmarkScreenRoot(
    viewModel: BookmarkViewModel = koinViewModel(),
    onBookmarkDeleted: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            BookmarkEvent.BookmarkDeleted -> onBookmarkDeleted()
            is BookmarkEvent.ShowError -> { /* Show snackbar */ }
        }
    }

    BookmarkScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun BookmarkScreen(
    state: BookmarkState,
    onAction: (BookmarkAction) -> Unit,
) {
    if (state.isLoading && state.bookmarks.isEmpty()) {
        LoadingIndicator()
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.bookmarks, key = { it.id }) { bookmark ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = bookmark.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = bookmark.url, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
```

```kotlin
// feature/bookmark/presentation/di/BookmarkPresentationModule.kt
package com.template.project.feature.bookmark.presentation.di

import com.template.project.feature.bookmark.presentation.BookmarkViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val bookmarkPresentationModule = module {
    viewModelOf(::BookmarkViewModel)
}
```

### Step 6: Wire ke composeApp

**Tambah dependency di `composeApp/build.gradle.kts`:**

```kotlin
commonMain.dependencies {
    // ... existing dependencies ...

    // Feature: Bookmark
    implementation(projects.feature.bookmark.domain)
    implementation(projects.feature.bookmark.data)
    implementation(projects.feature.bookmark.presentation)
}
```

**Register DI module di `AppModule.kt`:**

```kotlin
import com.template.project.feature.bookmark.data.di.bookmarkDataModule
import com.template.project.feature.bookmark.presentation.di.bookmarkPresentationModule

val appModule = module {
    includes(
        // Core
        coreDataModule,

        // ... existing modules ...

        // Bookmark
        bookmarkDataModule,
        bookmarkPresentationModule,
    )
}
```

**Tambahkan route di `App.kt`:**

```kotlin
composable<BookmarkRoute> {
    MainScaffold(navController = navController, currentRoute = BookmarkRoute) {
        BookmarkScreenRoot()
    }
}
```

### Step 7: Sync & Build

```bash
# Sync Gradle
./gradlew --refresh-dependencies

# Build untuk verifikasi
./gradlew assemble
```

✅ **Selesai!** Kamu sudah berhasil membuat feature module baru dari nol.

---

## 12. Testing

### 12.1 Pendekatan Testing

Template ini menggunakan:
- **Kotlin Test** (`kotlin.test`) — untuk assertions multiplatform
- **Turbine** — untuk testing `Flow` dan `StateFlow`
- **Fake implementations** — untuk mocking repository

### 12.2 Membuat Fake Repository

```kotlin
// feature/home/domain/src/commonTest/.../FakeProductRepository.kt
class FakeProductRepository : ProductRepository {

    var productsResult: Result<List<Product>, DataError.Network> =
        Result.Success(emptyList())
    var fetchCallCount = 0

    override suspend fun getProducts(limit: Int, skip: Int): Result<List<Product>, DataError.Network> {
        fetchCallCount++
        return productsResult
    }

    override suspend fun getProductById(id: Int): Result<Product, DataError.Network> {
        TODO("Implement if needed")
    }
}
```

### 12.3 Menulis ViewModel Test

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepository: FakeProductRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)  // ⚠️ WAJIB untuk ViewModel test
        fakeRepository = FakeProductRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()  // ⚠️ WAJIB cleanup
    }

    @Test
    fun successfulFetchUpdatesStateWithProducts() = runTest {
        // Given
        fakeRepository.productsResult = Result.Success(testProducts)
        viewModel = HomeViewModel(fakeRepository)

        // When & Then
        viewModel.state.test {  // 🔹 Turbine's test {}
            val initial = awaitItem()
            assertTrue(initial.products.isEmpty())

            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.products.size)
        }
    }

    @Test
    fun onProductClickEmitsNavigateEvent() = runTest {
        viewModel = HomeViewModel(fakeRepository)

        viewModel.events.test {
            viewModel.onAction(HomeAction.OnProductClick(productId = 42))
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertIs<HomeEvent.NavigateToDetail>(event)
            assertEquals(42, event.productId)
        }
    }
}
```

### 12.4 Menjalankan Test

```bash
# Jalankan semua test
./gradlew allTests

# Jalankan test module spesifik
./gradlew :feature:home:presentation:allTests

# Generate coverage report (HTML)
./gradlew koverHtmlReport
# Output: build/reports/kover/html/index.html

# Verify coverage threshold (gagal jika di bawah minimum)
./gradlew koverVerify
```

> 💡 **Tips Testing:**
> - Selalu gunakan `Dispatchers.setMain(testDispatcher)` di `@BeforeTest` dan `resetMain()` di `@AfterTest` (Cegah Deadlock ViewModel Turbine)
> - Selalu gunakan `Dispatchers.resetMain()` di `@AfterTest`
> - Gunakan `testDispatcher.scheduler.advanceUntilIdle()` untuk "menjalankan" semua pending coroutine
> - Gunakan Turbine `.test {}` untuk assert Flow emissions
> - **Gotcha Kover:** Pastikan ada minimal 1 test (dummy test diperbolehkan) agar task Kover tidak error "No tests discovered".
> - **Gotcha Turbine:** Cancel Unconsumed Events setelah assert Flow selesai jika ada background IO coroutine. Kover Android spoof di-handle otomatis oleh `KoverConventionPlugin`.

---

## 13. Konfigurasi BuildKonfig & Secrets

### 13.1 Cara Kerja

`BuildKonfig` membaca nilai dari `local.properties` saat **compile time** dan menjadikannya konstanta di kode Kotlin. Ini aman karena:
- `local.properties` tidak di-commit ke Git
- Nilai diakses sebagai konstanta, bukan hardcoded string

### 13.2 Menambah Secret Baru

**1. Tambahkan di `local.properties`:**

```properties
API_BASE_URL=https://dummyjson.com
FIREBASE_PROJECT_ID=your-project-id
NEW_API_KEY=abc123secret
```

**2. Tambahkan di `local.properties.example` (tanpa value asli!):**

```properties
NEW_API_KEY=your-api-key-here
```

**3. Referensikan di kode:**

```kotlin
import com.template.project.core.data.BuildKonfig

val apiKey = BuildKonfig.NEW_API_KEY
```

> ⚠️ **PERATURAN KETAT:**
> - ❌ **JANGAN** pernah commit `local.properties`
> - ❌ **JANGAN** hardcode API key di kode Kotlin
> - ✅ Simpan semua secrets di `local.properties`
> - ✅ Sertakan contoh (tanpa value) di `local.properties.example`

---

## 14. Menjalankan di Setiap Platform

### 14.1 Android

**Via Android Studio (Recommended):**
1. Buka project di Android Studio
2. Pilih run configuration `composeApp`
3. Pilih device/emulator
4. Klik ▶ Run

**Via Terminal:**
```bash
./gradlew :composeApp:assembleDebug

# Install ke device yang terhubung
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### 14.2 iOS (macOS Only)

**Via Xcode:**
1. Buka `iosApp/iosApp.xcodeproj` di Xcode
2. Pilih simulator (misalnya iPhone 16)
3. Klik ▶ Run

**Via Terminal:**
```bash
# 1. Build
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -configuration Debug build

# 2. Boot simulator (jika belum)
xcrun simctl boot "iPhone 16"

# 3. Install
xcrun simctl install booted \
  ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphonesimulator/TemplateKmp.app

# 4. Launch
xcrun simctl launch booted com.template.project.TemplateKmp
```

> 💡 **Tips iOS:**
> - Pastikan Xcode Command Line Tools sudah terinstall
> - Jika error "No matching destination", periksa `IPHONEOS_DEPLOYMENT_TARGET` di Xcode
> - Build pertama kali akan lama karena Kotlin/Native compilation

### 14.3 Desktop (JVM)

```bash
./gradlew :composeApp:run
```

Untuk membuat distributable (installer):
```bash
# macOS → .dmg
# Windows → .msi
# Linux → .deb
./gradlew :composeApp:createDistributable
```

---

## 15. Menuju Production

### 15.1 Checklist Pre-Release

```
[ ] ✅ ./gradlew build                              — Tidak ada error
[ ] ✅ ./gradlew allTests                            — Semua test pass
[ ] ✅ ./gradlew koverVerify                         — Coverage ≥ 60%
[ ] ✅ Pastikan semua TODO sudah dihandle
[ ] ✅ API keys sudah menggunakan BuildKonfig
[ ] ✅ Logging level sudah bukan LogLevel.ALL di release
[ ] ✅ local.properties tidak ter-commit
```

### 15.2 Android Release Build

**1. Buat keystore (sekali saja):**

```bash
keytool -genkey -v -keystore release-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-app-key
```

**2. Tambahkan signing config di `composeApp/build.gradle.kts`:**

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../release-keystore.jks")
            storePassword = "your-store-password"
            keyAlias = "my-app-key"
            keyPassword = "your-key-password"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

**3. Build:**

```bash
# APK (untuk testing di device)
./gradlew :composeApp:assembleRelease

# AAB (untuk upload ke Play Store) ← GUNAKAN INI!
./gradlew :composeApp:bundleRelease
```

> ⚠️ **PENTING:**
> - Selalu gunakan **AAB** (`bundleRelease`) untuk Play Store
> - **Jangan commit keystore** dan password — simpan secara aman
> - ProGuard **harus keep `@Serializable` classes** — sudah ada di `proguard-rules.pro`

### 15.3 ProGuard Rules

File `composeApp/proguard-rules.pro` sudah dikonfigurasi untuk menjaga:
- Kotlin Serialization classes
- Ktor classes
- Koin classes

Jika ada crash di release build setelah tambah library baru, periksa ProGuard rules.

### 15.4 Version Management

Update versi di `gradle/libs.versions.toml`:

```toml
# Project config — CHANGE THESE for your project
projectApplicationId = "com.yourcompany.yourapp"
projectVersionName = "1.0.0"
projectVersionCode = "1"
```

### 15.5 Ganti Package Name

Untuk mengganti dari `com.template.project` ke package name kamu:

1. **`libs.versions.toml`**: Ganti `projectApplicationId`
2. **Semua file Kotlin**: Ganti package declaration
3. **Android Manifest** (jika ada)
4. **`composeApp/build.gradle.kts`**: Ganti `namespace`
5. **Folder structure**: Rename folder dari `com/template/project` ke package baru
6. **`build-logic` plugins**: Update `TargetConfigurations.kt` jika perlu

> 💡 **Tips:** Gunakan fitur **Refactor → Rename Package** di Android Studio untuk otomatis rename.

---

## 16. Troubleshooting

### Problem & Solution

| Problem | Solusi |
|---------|--------|
| **Gradle sync gagal** | `File` → `Invalidate Caches` → Restart. Atau hapus folder `.gradle/` dan `.kotlin/` di root project |
| **"Unresolved reference: projects"** | Pastikan `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` ada di `settings.gradle.kts` |
| **DI resolution fails** | Pastikan `coreDataModule` di-include **sebelum** feature modules di `AppModule.kt` |
| **iOS build error** | Pastikan Xcode Command Line Tools terinstall: `xcode-select --install` |
| **HTTP request timeout** | Cek koneksi internet, cek base URL di `local.properties` |
| **Flow tidak emit data** | Pastikan menggunakan `SharingStarted.WhileSubscribed(5_000)` dan subscriber aktif |
| **ViewModel Test Deadlock/Hang** | Pastikan menggunakan `Dispatchers.setMain` dan cancel unconsumed turbine events |
| **Kover task error "No tests discovered"** | Tambahkan minimal satu *dummy test* di modul tersebut |
| **Token refresh loop** | Pastikan endpoint login/register di-skip dari auth: `sendWithoutRequest { ... }` |
| **`CancellationException` crash** | **Jangan pernah** catch `CancellationException` — selalu re-throw |
| **Room migration error** | Periksa schema directory dan migration steps |
| **Desktop crash** | Pastikan `kotlinx-coroutines-swing` ada di `desktopMain.dependencies` |
| **Release build crash** | Periksa ProGuard rules — `@Serializable` classes harus di-keep |
| **DataStore path error** | DataStore memerlukan `expect`/`actual` per platform untuk file path |

### Quick Debug Commands

```bash
# Lihat dependency tree
./gradlew :composeApp:dependencies

# Cek outdated dependencies
./gradlew dependencyUpdates

# Clean build
./gradlew clean build

# Force refresh dependencies
./gradlew --refresh-dependencies
```

---

## 17. Konvensi & Best Practices

### 17.1 Naming Conventions

| Komponen | Convention | Contoh |
|----------|-----------|--------|
| Repository Interface | `*Repository` | `ProductRepository` |
| Repository Impl | `Default*Repository` | `DefaultProductRepository` |
| ViewModel | `*ViewModel` | `HomeViewModel` |
| Screen Composable | `*ScreenRoot` + `*Screen` | `HomeScreenRoot`, `HomeScreen` |
| State | `*State` | `HomeState` |
| Action | `*Action` | `HomeAction` |
| Event | `*Event` | `HomeEvent` |
| DI Module | `*DataModule`, `*PresentationModule` | `homeDataModule` |
| DTO | `*Dto` | `ProductDto` |
| Mapper | `*Mapper` atau extension `toDomain()` | `ProductMapper.kt` |
| Route | `*Route` | `HomeRoute` |
| Navigation Graph | `*Graph` | `MainGraph`, `AuthGraph` |

### 17.2 Error Handling

```kotlin
// ✅ BENAR — Gunakan Result type
suspend fun getProducts(): Result<List<Product>, DataError.Network>

// ❌ SALAH — Jangan throw exception
suspend fun getProducts(): List<Product> // throws IOException
```

### 17.3 Logging

```kotlin
// ✅ BENAR — Kermit
import co.touchlab.kermit.Logger

Logger.d { "Products loaded: ${products.size}" }
Logger.e(exception) { "Failed to load products" }

// ❌ SALAH
println("Products loaded")
Log.d("TAG", "Products loaded")
```

### 17.4 State Management

```kotlin
// ✅ BENAR — WhileSubscribed dengan timeout
val state = _state
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState)

// ❌ SALAH — Eagerly (memory leak)
val state = _state.stateIn(viewModelScope, SharingStarted.Eagerly, initialState)
```

### 17.5 Coroutines

```kotlin
// ✅ BENAR — Re-throw CancellationException
try {
    httpClient.get(...)
} catch (e: CancellationException) {
    throw e  // WAJIB re-throw!
} catch (e: Exception) {
    Result.Error(DataError.Network.UNKNOWN)
}

// ❌ SALAH — Catch semua exception
try {
    httpClient.get(...)
} catch (e: Exception) {
    // CancellationException ikut ter-catch → coroutine tidak bisa di-cancel!
}
```

### 17.6 Git Commit Messages

Gunakan [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add bookmark feature module
fix: resolve token refresh infinite loop
refactor: extract common UI components to designsystem
test: add HomeViewModel unit tests
docs: update GUIDE.md with troubleshooting section
chore: update Kotlin to 2.3.0
```

---

<div align="center">

**🎉 Selamat!** Kamu sudah siap untuk mulai develop dengan TemplateKMP.

Jika ada pertanyaan, jangan ragu untuk membuka [Issue](https://github.com/firdaus1453/TemplateKMP/issues) di GitHub.

</div>
