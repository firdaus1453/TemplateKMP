# App Wiring Reference

Complete guide for wiring the composition root in `composeApp` — DI, navigation, platform entry points.

## 1. Koin DI — App Module

```kotlin
package com.mycompany.myapp

import com.mycompany.myapp.core.data.di.coreDataModule
import com.mycompany.myapp.feature.auth.presentation.di.authPresentationModule
// import com.mycompany.myapp.feature.chat.data.di.chatDataModule
// import com.mycompany.myapp.feature.chat.database.di.chatDatabaseModule
// import com.mycompany.myapp.feature.chat.presentation.di.chatPresentationModule
import org.koin.dsl.module

val appModule = module {
    // App-level dependencies can go here
}

// Combine ALL modules in correct order: core first, then features
val allModules = listOf(
    // Core
    coreDataModule,

    // Feature: Auth
    authPresentationModule,

    // Feature: Chat
    // chatDatabaseModule,
    // chatDataModule,
    // chatPresentationModule,

    // App
    appModule,
)
```

## 2. Navigation — NavHost setup

### Navigation routes (in each feature's presentation module)

```kotlin
// feature/auth/presentation/
@Serializable data object LoginRoute
@Serializable data object RegisterRoute

// feature/chat/presentation/
@Serializable data object ChatListRoute
@Serializable data class ChatDetailRoute(val chatId: String)
```

### NavigationRoot.kt (in composeApp)

```kotlin
package com.mycompany.myapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.mycompany.myapp.feature.auth.presentation.LoginRoute
import com.mycompany.myapp.feature.auth.presentation.RegisterRoute
import com.mycompany.myapp.feature.auth.presentation.login.LoginScreenRoot
// Import other screens and routes...
import kotlinx.serialization.Serializable

// Top-level graph route markers
@Serializable data object AuthGraph
@Serializable data object MainGraph

@Composable
fun NavigationRoot(
    isLoggedIn: Boolean,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) MainGraph else AuthGraph,
    ) {
        authGraph(navController)
        mainGraph(navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
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
            // RegisterScreenRoot(...)
        }
    }
}

private fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation<MainGraph>(startDestination = ChatListRoute) {
        composable<ChatListRoute> {
            // ChatListScreenRoot(...)
        }
        composable<ChatDetailRoute> { backStackEntry ->
            // val args = backStackEntry.toRoute<ChatDetailRoute>()
            // ChatDetailScreenRoot(chatId = args.chatId, ...)
        }
    }
}
```

## 3. App.kt — Common entry point

```kotlin
package com.mycompany.myapp

import androidx.compose.runtime.Composable
import com.mycompany.myapp.core.designsystem.AppTheme

@Composable
fun App(
    isLoggedIn: Boolean = false,
) {
    AppTheme {
        NavigationRoot(
            isLoggedIn = isLoggedIn,
        )
    }
}
```

## 4. Platform entry points

### Android — MainActivity.kt

```kotlin
package com.mycompany.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }
}
```

### Android — Application class

```kotlin
package com.mycompany.myapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(allModules)
        }
    }
}
```

### Android — AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".MyApp"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApp">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MyApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### iOS — MainViewController.kt

```kotlin
package com.mycompany.myapp

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
            modules(allModules)
        }
    }
) {
    App()
}
```

### Desktop — main.kt

```kotlin
package com.mycompany.myapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(allModules)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MyApp",
        ) {
            App()
        }
    }
}
```

## 5. iOS Xcode project setup

### iosApp/iosApp/iOSApp.swift

```swift
import SwiftUI

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### iosApp/iosApp/ContentView.swift

```swift
import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
```

## 6. Firebase push notifications (optional)

### Android — FirebaseMessagingService

```kotlin
package com.mycompany.myapp

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber // Use Timber on Android

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: ${message.data}")
        // Handle notification display
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM token refreshed: $token")
        // Send token to your backend
    }
}
```

Add to AndroidManifest.xml inside `<application>`:

```xml
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### iOS — Firebase setup in AppDelegate

Add Firebase SDK via Swift Package Manager (FirebaseMessaging package).
Add `GoogleService-Info.plist` to the Xcode project.

```swift
import Firebase

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        FirebaseApp.configure()
        return true
    }
}
```

## Summary of wiring order

1. Create Koin modules for each layer (core → feature → app)
2. Combine all modules in `allModules` list
3. Start Koin on each platform (Application.onCreate on Android, MainViewController on iOS, main() on Desktop)
4. Set up NavigationRoot with all screens and graph nesting
5. Call `App()` composable from each platform entry point
6. Platform-specific setup (splash screen, Firebase, permissions) goes in platform source sets
