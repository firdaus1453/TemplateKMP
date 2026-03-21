package com.template.project

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.template.project.core.designsystem.AppTheme
import com.template.project.feature.auth.presentation.LoginRoute
import com.template.project.feature.auth.presentation.login.LoginScreenRoot
import com.template.project.feature.home.presentation.HomeRoute
import com.template.project.feature.home.presentation.HomeScreenRoot
import com.template.project.feature.media.presentation.MediaRoute
import com.template.project.feature.media.presentation.MediaScreenRoot
import com.template.project.feature.notifications.presentation.NotificationsRoute
import com.template.project.feature.notifications.presentation.NotificationsScreenRoot
import com.template.project.feature.profile.presentation.ProfileRoute
import com.template.project.feature.profile.presentation.ProfileScreenRoot
import com.template.project.feature.search.presentation.SearchRoute
import com.template.project.feature.search.presentation.SearchScreenRoot
import com.template.project.feature.settings.presentation.SettingsRoute
import com.template.project.feature.settings.presentation.SettingsScreenRoot
import com.template.project.navigation.AuthGraph
import com.template.project.navigation.MainGraph
import org.koin.compose.KoinContext

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, HomeRoute),
    BottomNavItem("Search", Icons.Default.Search, SearchRoute),
    BottomNavItem("Notifications", Icons.Default.Notifications, NotificationsRoute),
    BottomNavItem("Profile", Icons.Default.Person, ProfileRoute),
    BottomNavItem("Settings", Icons.Default.Settings, SettingsRoute),
)

@Composable
fun App() {
    KoinContext {
        AppTheme {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = AuthGraph,
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
                            onRegisterClick = {
                                // Navigate to register or show message
                            },
                        )
                    }
                }

                // Main flow after auth
                navigation<MainGraph>(startDestination = HomeRoute) {
                    composable<HomeRoute> {
                        MainScaffold(navController = navController, currentRoute = HomeRoute) {
                            HomeScreenRoot(
                                onProductClick = { productId ->
                                    // Navigate to product detail
                                },
                            )
                        }
                    }

                    composable<SearchRoute> {
                        MainScaffold(navController = navController, currentRoute = SearchRoute) {
                            SearchScreenRoot()
                        }
                    }

                    composable<NotificationsRoute> {
                        MainScaffold(navController = navController, currentRoute = NotificationsRoute) {
                            NotificationsScreenRoot()
                        }
                    }

                    composable<ProfileRoute> {
                        MainScaffold(navController = navController, currentRoute = ProfileRoute) {
                            ProfileScreenRoot(
                                onLogout = {
                                    navController.navigate(AuthGraph) {
                                        popUpTo(MainGraph) { inclusive = true }
                                    }
                                },
                            )
                        }
                    }

                    composable<SettingsRoute> {
                        MainScaffold(navController = navController, currentRoute = SettingsRoute) {
                            SettingsScreenRoot()
                        }
                    }

                    composable<MediaRoute> {
                        MainScaffold(navController = navController, currentRoute = MediaRoute) {
                            MediaScreenRoot()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navController: androidx.navigation.NavHostController,
    currentRoute: Any,
    content: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hasRoute(item.route::class) == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(paddingValues),
        ) {
            content()
        }
    }
}