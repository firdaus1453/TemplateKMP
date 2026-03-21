# Testing Reference

Complete testing patterns for KMP/CMP applications — unit tests, integration tests, Kover coverage, and Chucker HTTP debugging.

## 1. Kover Setup (Coverage Reports)

[kotlinx-kover](https://github.com/Kotlin/kotlinx-kover) generates code coverage reports for Kotlin Multiplatform.

### Add to version catalog (`libs.versions.toml`)

```toml
[versions]
kover = "0.9.1"

[plugins]
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

### KoverConventionPlugin

Create `build-logic/convention/src/main/kotlin/KoverConventionPlugin.kt`:

```kotlin
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")

            extensions.configure<KoverProjectExtension> {
                reports {
                    filters {
                        excludes {
                            // Exclude generated code from coverage
                            classes(
                                "*_Factory",
                                "*_HiltModules*",
                                "*BuildKonfig*",
                                "*ComposableSingletons*",
                                "*.di.*Module*",
                                "*_Impl",
                                "*_Impl\$*",
                            )
                            packages(
                                "*.di",
                                "*.theme",
                                "*.designsystem",
                            )
                        }
                    }

                    verify {
                        rule("Minimum coverage") {
                            minBound(60) // 60% minimum coverage
                        }
                    }
                }
            }
        }
    }
}
```

Register in `build-logic/convention/build.gradle.kts`:

```kotlin
gradlePlugin {
    plugins {
        register("kover") {
            id = "convention.kover"
            implementationClass = "KoverConventionPlugin"
        }
    }
}
```

### Apply per-module

```kotlin
// feature/chat/data/build.gradle.kts
plugins {
    id("convention.kmp.library")
    id("convention.kover")
}
```

### Root aggregation (root `build.gradle.kts`)

```kotlin
plugins {
    alias(libs.plugins.kover)
}

dependencies {
    // Add all modules for aggregated coverage report
    kover(projects.core.data)
    kover(projects.core.domain)
    kover(projects.feature.chat.data)
    kover(projects.feature.chat.domain)
    kover(projects.feature.chat.presentation)
    kover(projects.feature.auth.data)
    kover(projects.feature.auth.domain)
    kover(projects.feature.auth.presentation)
    // ... all other modules
}
```

### Run coverage reports

```bash
# Generate HTML report
./gradlew koverHtmlReport

# Generate XML report (for CI tools)
./gradlew koverXmlReport

# Verify coverage meets minimum bound
./gradlew koverVerify

# Run all tests + coverage
./gradlew allTests koverHtmlReport
```

Reports are generated in `build/reports/kover/html/index.html`.

---

## 2. Unit Testing Patterns

### Test dependencies (`libs.versions.toml`)

```toml
[versions]
kotlinxCoroutinesTest = "1.9.0"
turbine = "1.2.0"

[libraries]
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTest" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
```

### KmpLibraryConventionPlugin (add test deps)

```kotlin
// Inside KmpLibraryConventionPlugin, in sourceSets block:
commonTest.dependencies {
    implementation(libs.findLibrary("kotlin.test").get())
    implementation(libs.findLibrary("kotlinx.coroutines.test").get())
    implementation(libs.findLibrary("turbine").get())
}
```

### Test directory structure

```
feature/chat/data/src/
├── commonMain/kotlin/...          # Production code
└── commonTest/kotlin/
    └── com/mycompany/myapp/feature/chat/data/
        ├── FakeChatService.kt     # Fake network service
        ├── FakeChatDatabase.kt    # Fake database
        └── OfflineFirstChatRepositoryTest.kt
```

### Fake/Stub pattern for repositories

```kotlin
package com.mycompany.myapp.feature.chat.data

import com.mycompany.myapp.core.domain.util.DataError
import com.mycompany.myapp.core.domain.util.Result
import com.mycompany.myapp.feature.chat.domain.Chat
import com.mycompany.myapp.feature.chat.domain.ChatService

class FakeChatService : ChatService {

    var chatsToReturn: Result<List<Chat>, DataError.Remote> =
        Result.Success(emptyList())

    var fetchCallCount = 0
        private set

    override suspend fun getChats(): Result<List<Chat>, DataError.Remote> {
        fetchCallCount++
        return chatsToReturn
    }

    // ... stub other methods
}
```

### ViewModel unit test

```kotlin
package com.mycompany.myapp.feature.chat.presentation

import app.cash.turbine.test
import com.mycompany.myapp.core.domain.util.Result
import com.mycompany.myapp.feature.chat.data.FakeChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    private lateinit var viewModel: ChatListViewModel
    private lateinit var fakeChatRepository: FakeChatRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeChatRepository = FakeChatRepository()
        viewModel = ChatListViewModel(fakeChatRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows loading`() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
        }
    }

    @Test
    fun `successful fetch updates state with chats`() = runTest {
        val testChats = listOf(
            Chat(id = "1", name = "Chat 1"),
            Chat(id = "2", name = "Chat 2"),
        )
        fakeChatRepository.chatsFlow.value = testChats

        viewModel.state.test {
            skipItems(1) // skip initial loading state
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.chats.size)
            assertEquals("Chat 1", loaded.chats[0].name)
        }
    }

    @Test
    fun `refresh triggers new fetch`() = runTest {
        viewModel.onAction(ChatListAction.OnRefresh)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, fakeChatRepository.fetchCallCount) // initial + refresh
    }
}
```

### Domain layer unit test (pure Kotlin)

```kotlin
package com.mycompany.myapp.core.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun `map transforms success value`() {
        val result: Result<Int, DataError> = Result.Success(42)
        val mapped = result.map { it.toString() }
        assertTrue(mapped is Result.Success)
        assertEquals("42", (mapped as Result.Success).data)
    }

    @Test
    fun `map preserves error on failure`() {
        val result: Result<Int, DataError.Remote> =
            Result.Error(DataError.Remote.SERVER_ERROR)
        val mapped = result.map { it.toString() }
        assertTrue(mapped is Result.Error)
    }
}
```

---

## 3. Integration / E2E Test Patterns

### Navigation integration test

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class NavigationTest {

    @Test
    fun `login success navigates to chat list`() = runTest {
        val fakeAuthService = FakeAuthService().apply {
            loginResult = Result.Success(testAuthInfo)
        }
        val fakeSessionStorage = FakeSessionStorage()
        val viewModel = LoginViewModel(fakeAuthService, fakeSessionStorage)

        viewModel.events.test {
            viewModel.onAction(LoginAction.OnLoginClick)
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is LoginEvent.LoginSuccess)
        }
    }
}
```

### Running all tests

```bash
# Run all multiplatform tests
./gradlew allTests

# Run tests for specific module
./gradlew :feature:chat:data:allTests

# Run only JVM/Android tests
./gradlew :feature:chat:data:testDebugUnitTest

# Run with coverage
./gradlew allTests koverHtmlReport
```

---

## 4. Chucker Integration (Android-only HTTP Inspector)

[Chucker](https://github.com/ChuckerTeam/chucker) intercepts HTTP traffic and displays it in a notification for debugging.

### Add to version catalog

```toml
[versions]
chucker = "4.1.0"

[libraries]
chucker-debug = { module = "com.github.chuckerteam.chucker:library", version.ref = "chucker" }
chucker-release = { module = "com.github.chuckerteam.chucker:library-no-op", version.ref = "chucker" }
```

### HttpClientEngine expect/actual with Chucker

Since Chucker is Android-only, integrate it in the `androidMain` platform source set of `core/data`:

```kotlin
// core/data/src/androidMain/kotlin/.../HttpEngineFactory.kt
package com.mycompany.myapp.core.data.networking

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual class HttpEngineFactory(
    private val context: Context,
) {
    actual fun create(): HttpClientEngine {
        return OkHttp.create {
            // Add Chucker interceptor for debug builds
            val chuckerInterceptor = ChuckerInterceptor.Builder(context)
                .collector(
                    ChuckerCollector(
                        context = context,
                        showNotification = true,
                        retentionPeriod = RetentionManager.Period.ONE_HOUR
                    )
                )
                .maxContentLength(250_000L)
                .redactHeaders("Authorization", "x-api-key") // Redact sensitive headers
                .alwaysReadResponseBody(true)
                .build()

            addInterceptor(chuckerInterceptor)
        }
    }
}
```

```kotlin
// core/data/src/commonMain/kotlin/.../HttpEngineFactory.kt
expect class HttpEngineFactory {
    fun create(): HttpClientEngine
}
```

```kotlin
// core/data/src/iosMain/kotlin/.../HttpEngineFactory.kt
actual class HttpEngineFactory {
    actual fun create(): HttpClientEngine {
        return Darwin.create()
    }
}
```

```kotlin
// core/data/src/desktopMain/kotlin/.../HttpEngineFactory.kt
actual class HttpEngineFactory {
    actual fun create(): HttpClientEngine {
        return OkHttp.create()
    }
}
```

### Gradle: debug vs release dependency

```kotlin
// core/data/build.gradle.kts
kotlin {
    sourceSets {
        androidMain.dependencies {
            // Use debug variant in debug builds, no-op in release
            implementation(libs.chucker.debug)
        }
    }
}
```

> **Tip**: For release builds, swap `libs.chucker.debug` with `libs.chucker.release` using build variant configuration, or use `debugImplementation` and `releaseImplementation` in the Android block:

```kotlin
// Alternative: Android-style build type separation
android {
    // ...
}

dependencies {
    debugImplementation(libs.chucker.debug)
    releaseImplementation(libs.chucker.release)
}
```

### Key Chucker points:
- **Android-only** — other platforms don't need it
- **Redact sensitive headers** like `Authorization` and `x-api-key`
- **No-op library** for release builds prevents any performance impact
- Shows HTTP traffic via notification on the device — very useful for debugging

---

## 5. Testing checklist

- [ ] Every ViewModel has a corresponding `*ViewModelTest` class
- [ ] Every repository has a corresponding `*RepositoryTest` class
- [ ] Domain models and `Result` utilities have unit tests
- [ ] Fakes/Stubs created for all interfaces (services, repositories, DAOs)
- [ ] Use `Dispatchers.setMain(testDispatcher)` in `@BeforeTest`
- [ ] Use `Dispatchers.resetMain()` in `@AfterTest`
- [ ] Use Turbine for testing `Flow` and `Channel` emissions
- [ ] Kover applied to all testable modules
- [ ] Kover minimum coverage bound set (recommended: 60%+)
- [ ] Root `build.gradle.kts` aggregates all modules for coverage
- [ ] Chucker integrated for Android debug HTTP inspection
- [ ] Chucker no-op used for release builds
- [ ] Coverage report generated: `./gradlew allTests koverHtmlReport`
