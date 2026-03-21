# Project Setup Reference

Complete file templates for initializing a KMP/CMP project from scratch.

## 1. settings.gradle.kts

```kotlin
rootProject.name = "MyApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// App entry point
include(":composeApp")

// Core modules
include(":core:presentation")
include(":core:domain")
include(":core:data")
include(":core:designsystem")

// Feature modules — add new features here
// include(":feature:auth:presentation")
// include(":feature:auth:domain")
// include(":feature:chat:presentation")
// include(":feature:chat:domain")
// include(":feature:chat:data")
// include(":feature:chat:database")
```

## 2. gradle/libs.versions.toml

```toml
[versions]
# Build tools
agp = "8.11.1"
kotlin = "2.2.0"
ksp = "2.2.0-2.0.2"
google-services = "4.4.3"

# Compose
compose-multiplatform = "1.9.0-beta01"
compose-lifecycle = "2.9.1"
navigation-compose = "2.9.0-beta04"
jetbrains-core-bundle = "1.0.1"
material-icons = "1.7.3"

# AndroidX
androidx-activity = "1.10.1"
androidx-core = "1.16.0"
core-splashscreen = "1.0.1"
appcompatVersion = "1.7.1"

# Kotlinx
kotlinx-coroutines = "1.10.2"
kotlinx-serialization = "1.9.0"
kotlinx-datetime = "0.7.1"

# Third party
koin = "4.1.0"
ktor = "3.2.3"
room = "2.7.2"
sqlite = "2.5.2"
datastore = "1.1.7"
firebase-bom = "34.0.0"
coil = "3.3.0"
coil-network = "3.3.0"
moko = "0.19.1"
buildkonfig = "0.17.1"
kermit = "2.0.6"
kover = "0.9.1"
chucker = "4.1.0"
kotlinxCoroutinesTest = "1.10.2"
turbine = "1.2.0"

# Desktop
jsystemthemedetector = "3.9.1"

# Android desugar
androidDesugarJdkLibs = "2.1.5"
androidTools = "31.12.0"

# Project config
projectApplicationId = "com.mycompany.myapp"
projectVersionName = "1.0"
projectMinSdkVersion = "26"
projectTargetSdkVersion = "36"
projectCompileSdkVersion = "36"
projectVersionCode = "1"

# Compose additional
compose-jetbrains = "1.8.2"
jetbrains-savedstate = "1.3.1"
adaptive = "1.2.0-alpha04"

[libraries]
# Kotlin
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }

# AndroidX Core
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidx-core" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompatVersion" }
core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "core-splashscreen" }

# Compose — Jetbrains multiplatform
jetbrains-compose-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "compose-lifecycle" }
jetbrains-compose-navigation = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
jetbrains-compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "compose-jetbrains" }
jetbrains-compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "compose-jetbrains" }
jetbrains-compose-material-icons-core = { module = "org.jetbrains.compose.material:material-icons-core", version.ref = "material-icons" }
jetbrains-compose-material-icons-extended = { module = "org.jetbrains.compose.material:material-icons-extended", version.ref = "material-icons" }
jetbrains-compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "compose-jetbrains" }
jetbrains-compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "compose-jetbrains" }
jetbrains-compose-backhandler = { module = "org.jetbrains.compose.ui:ui-backhandler", version.ref = "compose-multiplatform" }

# Lifecycle
jetbrains-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel", version.ref = "compose-lifecycle" }
jetbrains-lifecycle-compose = { group = "org.jetbrains.androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "compose-lifecycle" }
jetbrains-lifecycle-viewmodel-savedstate = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate", version.ref = "compose-lifecycle" }
jetbrains-savedstate = { module = "org.jetbrains.androidx.savedstate:savedstate", version.ref = "jetbrains-savedstate" }
jetbrains-bundle = { module = "org.jetbrains.androidx.core:core-bundle", version.ref = "jetbrains-core-bundle" }

# Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
koin-core-viewmodel = { group = "io.insert-koin", name = "koin-core-viewmodel", version.ref = "koin" }
koin-androidx-compose = { group = "io.insert-koin", name = "koin-androidx-compose", version.ref = "koin" }
koin-androidx-navigation = { group = "io.insert-koin", name = "koin-androidx-navigation", version.ref = "koin" }
koin-bom = { group = "io.insert-koin", name = "koin-bom", version.ref = "koin" }

# Ktor
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-auth = { module = "io.ktor:ktor-client-auth", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }

# DataStore
datastore = { module = "androidx.datastore:datastore", version.ref = "datastore" }
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# Material3 Adaptive
material3-adaptive = { module = "org.jetbrains.compose.material3.adaptive:adaptive", version.ref = "adaptive" }
material3-adaptive-layout = { module = "org.jetbrains.compose.material3.adaptive:adaptive-layout", version.ref = "adaptive" }
material3-adaptive-navigation = { module = "org.jetbrains.compose.material3.adaptive:adaptive-navigation", version.ref = "adaptive" }

# Room
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }
androidx-room-gradle-plugin = { module = "androidx.room:room-gradle-plugin", version.ref = "room" }

# Firebase
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-messaging = { module = "com.google.firebase:firebase-messaging" }

# Coil
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil-network" }

# Permissions
moko-permissions = { module = "dev.icerock.moko:permissions", version.ref = "moko" }
moko-permissions-compose = { module = "dev.icerock.moko:permissions-compose", version.ref = "moko" }
moko-permissions-notifications = { module = "dev.icerock.moko:permissions-notifications", version.ref = "moko" }

# Logging
touchlab-kermit = { module = "co.touchlab:kermit", version.ref = "kermit" }

# Desktop
jsystemthemedetector = { module = "com.github.Dansoftowner:jSystemThemeDetector", version.ref = "jsystemthemedetector" }

# Build logic dependencies
android-desugarJdkLibs = { module = "com.android.tools:desugar_jdk_libs", version.ref = "androidDesugarJdkLibs" }
android-gradlePlugin = { group = "com.android.tools.build", name = "gradle", version.ref = "agp" }
android-tools-common = { group = "com.android.tools", name = "common", version.ref = "androidTools" }
compose-gradlePlugin = { group = "org.jetbrains.kotlin", name = "compose-compiler-gradle-plugin", version.ref = "kotlin" }
kotlin-gradlePlugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
ksp-gradlePlugin = { group = "com.google.devtools.ksp", name = "com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }
buildkonfig-gradlePlugin = { group = "com.codingfeline.buildkonfig", name = "buildkonfig-gradle-plugin", version.ref = "buildkonfig" }
buildkonfig-compiler = { group = "com.codingfeline.buildkonfig", name = "buildkonfig-compiler", version.ref = "buildkonfig" }

# Testing
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTest" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

# Chucker (Android HTTP inspector)
chucker-debug = { module = "com.github.chuckerteam.chucker:library", version.ref = "chucker" }
chucker-release = { module = "com.github.chuckerteam.chucker:library-no-op", version.ref = "chucker" }

# Preview/debug
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }

[plugins]
# Convention plugins (defined in build-logic)
convention-cmp-application = { id = "com.mycompany.convention.cmp.application", version = "unspecified" }
convention-kmp-library = { id = "com.mycompany.convention.kmp.library", version = "unspecified" }
convention-cmp-library = { id = "com.mycompany.convention.cmp.library", version = "unspecified" }
convention-cmp-feature = { id = "com.mycompany.convention.cmp.feature", version = "unspecified" }
convention-buildkonfig = { id = "com.mycompany.convention.buildkonfig", version = "unspecified" }
convention-room = { id = "com.mycompany.convention.room", version = "unspecified" }

# Android
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
android-kotlin-multiplatform-library = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }

# Kotlin
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }

# Compose
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

# Build tools
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
google-services = { id = "com.google.gms.google-services", version.ref = "google-services" }
buildkonfig = { id = "com.codingfeline.buildkonfig", version.ref = "buildkonfig" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }

# Convention: testing
convention-kover = { id = "com.mycompany.convention.kover", version = "unspecified" }

[bundles]
koin-common = [
    "koin-core",
    "koin-compose",
    "koin-compose-viewmodel"
]

ktor-common = [
    "ktor-client-core",
    "ktor-client-content-negotiation",
    "ktor-serialization-kotlinx-json",
    "ktor-client-auth",
    "ktor-client-logging"
]
```

**IMPORTANT**: Replace `com.mycompany.myapp` and `com.mycompany.convention` with your actual package/application ID throughout.

## 3. Root build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kover)
}

// Kover: aggregate coverage from all modules
dependencies {
    kover(projects.core.data)
    kover(projects.core.domain)
    // Add all feature modules here for aggregated coverage
    // kover(projects.feature.chat.data)
    // kover(projects.feature.chat.domain)
    // kover(projects.feature.chat.presentation)
}
```

## 4. build-logic setup

### build-logic/settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
```

### build-logic/gradle.properties

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
```

### build-logic/convention/build.gradle.kts

```kotlin
plugins {
    `kotlin-dsl`
}

group = "com.mycompany.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.androidx.room.gradle.plugin)
    compileOnly(libs.buildkonfig.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("cmpApplication") {
            id = "com.mycompany.convention.cmp.application"
            implementationClass = "CmpApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = "com.mycompany.convention.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = "com.mycompany.convention.cmp.library"
            implementationClass = "CmpLibraryConventionPlugin"
        }
        register("cmpFeature") {
            id = "com.mycompany.convention.cmp.feature"
            implementationClass = "CmpFeatureConventionPlugin"
        }
        register("room") {
            id = "com.mycompany.convention.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("buildKonfig") {
            id = "com.mycompany.convention.buildkonfig"
            implementationClass = "BuildKonfigConventionPlugin"
        }
    }
}
```

## 5. gradle.properties (root)

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.parallel=true

kotlin.code.style=official
kotlin.daemon.jvmargs=-Xmx4096m

android.useAndroidX=true
android.nonTransitiveRClass=true
```

## 6. Module build.gradle.kts patterns

Each module uses a single convention plugin line:

### core/domain/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.kmp.library)
}
```

### core/data/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)

            implementation(libs.bundles.ktor.common)
            implementation(libs.datastore.preferences)
            implementation(libs.touchlab.kermit)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
```

### core/designsystem/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.cmp.library)
}
```

### core/presentation/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
        }
    }
}
```

### feature/auth/domain/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}
```

### feature/auth/presentation/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.cmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.auth.domain)
        }
    }
}
```

### feature/chat/data/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.chat.domain)
            implementation(projects.core.domain)
            implementation(projects.core.data)

            implementation(libs.bundles.ktor.common)
            implementation(libs.touchlab.kermit)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
```

### feature/chat/database/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.chat.domain)
            implementation(projects.core.domain)
        }
    }
}
```

### composeApp/build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.convention.cmp.application)
    alias(libs.plugins.google.services)
}

version = "1.0.0"

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.core.splashscreen)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            // All core modules
            implementation(projects.core.data)
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
            implementation(projects.core.presentation)

            // All feature modules
            implementation(projects.feature.auth.domain)
            implementation(projects.feature.auth.presentation)
            // implementation(projects.feature.chat.data)
            // implementation(projects.feature.chat.database)
            // implementation(projects.feature.chat.domain)
            // implementation(projects.feature.chat.presentation)

            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.bundles.koin.common)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.jetbrains.compose.viewmodel)
            implementation(libs.jetbrains.lifecycle.compose)
        }
        desktopMain.dependencies {
            implementation(projects.core.presentation)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlin.stdlib)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.mycompany.myapp.MainKt"
        nativeDistributions {
            packageName = "com.mycompany.myapp"
        }
    }
}
```
