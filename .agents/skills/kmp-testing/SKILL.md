---
name: kmp-testing
description: >
  Unit testing, Turbine Flow testing, in-memory Fake repositories, Kover code coverage, and Maestro automated UI testing for KMP applications. Trigger when: writing unit tests in commonTest / desktopTest, using Turbine test {}, mocking coroutine dispatchers, running Kover coverage, or creating/executing Maestro E2E test flows (.maestro/).
---

# Multiplatform Testing & Maestro Automation

This document defines testing patterns for ViewModel testing with Turbine, in-memory Fake repositories, Kover code coverage, and 100% automated headless UI testing with Maestro.

---

## 🧪 Testing Stack

| Tool | Purpose |
|---|---|
| **`kotlin.test`** | Multiplatform assertions (`assertEquals`, `assertTrue`, `assertIs`) |
| **`app.cash.turbine`** | Testing Coroutine `StateFlow` and `Channel` emissions |
| **`kotlinx-coroutines-test`** | Coroutine test control (`runTest`, `StandardTestDispatcher`, `advanceUntilIdle`) |
| **`kover`** | Code coverage reporting across multiplatform modules |
| **`maestro`** | Automated headless end-to-end UI testing for AI agents & CI/CD |

---

## 🚦 ViewModel Testing with Turbine

Always inject a `StandardTestDispatcher` and set it as `Dispatchers.Main` to prevent deadlocks:

```kotlin
import app.cash.turbine.test
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
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepository: FakeProductRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeProductRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetching products emits initial and loaded state`() = runTest {
        fakeRepository.productsResult = Result.Success(listOf(sampleProduct))
        viewModel = HomeViewModel(fakeRepository)

        viewModel.state.test {
            val initial = awaitItem()
            assertTrue(initial.products.isEmpty())

            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(1, loaded.products.size)
        }
    }

    @Test
    fun `clicking product emits navigation event`() = runTest {
        viewModel = HomeViewModel(fakeRepository)

        viewModel.events.test {
            viewModel.onAction(HomeAction.OnProductClick(42))
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertEquals(HomeEvent.NavigateToDetail(42), event)
        }
    }
}
```

---

## 🗃️ In-Memory Fake Repositories (Prefer over Mocks)

```kotlin
class FakeProductRepository : ProductRepository {
    var productsResult: Result<List<Product>, DataError.Network> = Result.Success(emptyList())

    override suspend fun getProducts(): Result<List<Product>, DataError.Network> {
        return productsResult
    }
}
```

---

## 🤖 Automated Headless UI Testing with Maestro

Maestro flows live in `.maestro/flows/` and can be executed headlessly by AI or CI:

```bash
# Run all automated test flows
./scripts/run_maestro_tests.sh

# Or run via root Gradle task
./gradlew maestroTest

# Run a specific flow
./scripts/run_maestro_tests.sh .maestro/flows/02_auth_flow.yaml
```

### Example Maestro Flow (`.maestro/flows/02_auth_flow.yaml`)
```yaml
appId: com.template.project
---
- launchApp:
    clearState: true

- assertVisible: "Welcome Back"

- tapOn: "Username"
- inputText: "emilys"

- tapOn: "Password"
- inputText: "emilyspass"

- hideKeyboard

- tapOn: "Sign In"

- extendedWaitUntil:
    visible: "Home"
    timeout: 10000
```
