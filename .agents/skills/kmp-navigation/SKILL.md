---
name: kmp-navigation
description: >
  Type-safe navigation conventions using Navigation Compose Multiplatform with Kotlinx Serialization. Trigger when: declaring @Serializable route objects or classes, defining nested NavGraphs, configuring NavHost in composeApp, passing typed route arguments, or managing backstack operations (popUpTo, singleTop, restoreState).
---

# Type-Safe Navigation Compose Multiplatform

This document defines type-safe routing patterns using `androidx.navigation.compose` and `kotlinx.serialization`.

---

## 🧭 Principles of Type-Safe Navigation

1. **`@Serializable` Routes**: Every destination and graph is represented by a type-safe `@Serializable` data object or data class.
2. **No String URL Routes**: Avoid string-based route definitions (e.g. `"home_screen/{id}"`).
3. **Graph Segregation**: Group related destinations into nested navigation graphs (`AuthGraph`, `MainGraph`, `CheckoutGraph`).
4. **Centralized Composition**: All feature routes are wired together in `:composeApp` (`App.kt`).

---

## 📌 Route & Graph Definitions

Routes are declared in their respective feature presentation modules or navigation interfaces:

```kotlin
import kotlinx.serialization.Serializable

// Feature Auth Routes
@Serializable
data object AuthGraph

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

// Feature Main / Tabs Routes
@Serializable
data object MainGraph

@Serializable
data object HomeRoute

@Serializable
data object SearchRoute

@Serializable
data object ProfileRoute

@Serializable
data object SettingsRoute

// Route with Arguments
@Serializable
data class ProductDetailRoute(val productId: Int)
```

---

## 🗺️ NavHost Wiring (`composeApp/src/commonMain/kotlin/.../App.kt`)

```kotlin
@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthGraph,
    ) {
        // 1. Auth Flow
        navigation<AuthGraph>(startDestination = LoginRoute) {
            composable<LoginRoute> {
                LoginScreenRoot(
                    onLoginSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(RegisterRoute)
                    }
                )
            }
            composable<RegisterRoute> {
                RegisterScreenRoot(
                    onRegisterSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // 2. Main Authenticated Flow
        navigation<MainGraph>(startDestination = HomeRoute) {
            composable<HomeRoute> {
                MainScaffold(navController = navController, currentRoute = HomeRoute) {
                    HomeScreenRoot(
                        onProductClick = { id ->
                            navController.navigate(ProductDetailRoute(id))
                        }
                    )
                }
            }

            composable<ProductDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ProductDetailRoute>()
                ProductDetailScreenRoot(
                    productId = route.productId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<SearchRoute> {
                MainScaffold(navController = navController, currentRoute = SearchRoute) {
                    SearchScreenRoot()
                }
            }

            composable<ProfileRoute> {
                MainScaffold(navController = navController, currentRoute = ProfileRoute) {
                    ProfileScreenRoot(
                        onLogout = {
                            navController.navigate(AuthGraph) {
                                popUpTo(MainGraph) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable<SettingsRoute> {
                MainScaffold(navController = navController, currentRoute = SettingsRoute) {
                    SettingsScreenRoot()
                }
            }
        }
    }
}
```

---

## 🔄 Bottom Navigation Backstack State Management

For bottom navigation tabs, preserve tab backstack states:

```kotlin
navController.navigate(item.route) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```
