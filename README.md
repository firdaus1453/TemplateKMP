<div align="center">

# 🏗️ TemplateKMP

**Production-ready Kotlin Multiplatform template with Clean Architecture**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.10.0-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-Android_|_iOS_|_Desktop-green)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

*A scalable, multi-module KMP/CMP template — ready to build your next cross-platform app.*

</div>

---

## ✨ Features

- 🎯 **Multi-module Clean Architecture** — Domain, Data, Presentation separation per feature
- 🧩 **Convention Plugins** — 9 pre-built Gradle plugins for consistent module configuration
- 🔐 **Authentication Ready** — Session storage, Bearer auth, auto token refresh via Ktor
- 🎨 **Design System** — Centralized theme, typography, and reusable UI components
- 📱 **MVI Pattern** — Unidirectional data flow with ViewModel + StateFlow
- 🗄️ **Room Database** — Offline-first ready with convention plugin
- 🧪 **Testing Infrastructure** — Unit tests with Turbine + Kover code coverage
- 🌐 **Ktor Networking** — Type-safe HTTP client with error handling
- 💉 **Koin DI** — Lightweight dependency injection across all platforms
- 🖼️ **Coil Image Loading** — Multiplatform image loading and caching
- 🔧 **BuildKonfig** — Secure build-time constants from `local.properties`

---

## 🏛️ Architecture

```
TemplateKMP/
├── 🔧 build-logic/convention/         # 9 Gradle convention plugins
├── 📱 androidApp/                      # Pure Android Entry Point
├── 📱 composeApp/                      # Composition root & Shared UI (Android, iOS, Desktop)
├── 🏗️ core/
│   ├── domain/                         # Pure Kotlin: Result, DataError, interfaces
│   ├── data/                           # Ktor, DataStore, SessionStorage
│   ├── presentation/                   # UiText, shared composables
│   └── designsystem/                   # AppTheme, colors, typography
├── 🧩 feature/
│   ├── auth/        (domain, presentation)
│   ├── home/        (domain, data, presentation)
│   ├── profile/     (domain, data, presentation)
│   ├── settings/    (domain, data, presentation)
│   ├── search/      (domain, data, presentation)
│   ├── notifications/ (domain, presentation)
│   └── media/       (domain, presentation)
├── 📄 gradle/libs.versions.toml
└── ⚙️ settings.gradle.kts
```

### Dependency Rules

```
androidApp → composeApp
composeApp → core/* + feature/*/
feature/*/presentation → feature/*/domain + core/presentation + core/designsystem
feature/*/data → feature/*/domain + core/domain + core/data
core/data → core/domain
core/presentation → core/domain + core/designsystem
```

> **Key Principles:**
> - `domain` = pure Kotlin, **ZERO** framework imports
> - `data` implements domain interfaces
> - `presentation` depends on domain only, **never** on data
> - Feature modules **NEVER** depend on other feature modules
> - `composeApp` wires all DI and navigation

---

## 🔧 Convention Plugins

| Plugin | ID | Purpose |
|--------|----|---------|
| `KmpLibraryConventionPlugin` | `template.kmp.library` | Pure Kotlin Multiplatform library module |
| `CmpLibraryConventionPlugin` | `template.cmp.library` | Compose Multiplatform library module |
| `CmpFeatureConventionPlugin` | `template.cmp.feature` | Feature presentation module (Compose + ViewModel) |
| `CmpApplicationConventionPlugin` | `template.cmp.application` | Main app module (Android + iOS + Desktop) |
| `AndroidApplicationConventionPlugin` | `template.android.application` | Android application base config |
| `AndroidApplicationComposeConventionPlugin` | `template.android.application.compose` | Android Compose integration |
| `KoverConventionPlugin` | `template.kover` | Kover test coverage reporting |
| `RoomConventionPlugin` | `template.room` | Room database with KSP |
| `BuildKonfigConventionPlugin` | `template.buildkonfig` | Build-time constants from local.properties |

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| [AGP](https://developer.android.com/studio/releases/gradle-plugin) | 9.1+ | Android build tools |
| [Kotlin](https://kotlinlang.org) | 2.3.0 | Language |
| [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) | 1.10.0 | UI framework |
| [Koin](https://insert-koin.io) | 4.1.0 | Dependency injection |
| [Ktor](https://ktor.io) | 3.2.3 | HTTP client |
| [Room](https://developer.android.com/kotlin/multiplatform/room) | 2.7.2 | Database (offline-first) |
| [Navigation Compose](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html) | 2.9.0 | Navigation |
| [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) | 1.1.7 | Session storage |
| [Coil](https://coil-kt.github.io/coil/) | 3.3.0 | Image loading |
| [Kover](https://github.com/Kotlin/kotlinx-kover) | 0.9.1 | Test coverage |
| [Turbine](https://github.com/cashapp/turbine) | 1.2.0 | Flow testing |
| [Kermit](https://github.com/touchlab/Kermit) | 2.0.6 | Multiplatform logging |
| [BuildKonfig](https://github.com/nickcaballero/buildkonfig-kmp) | 0.17.1 | Build constants |

---

## 📖 Developer Guide

**New to this template?** Check out our comprehensive **[Developer Guide (GUIDE.md)](GUIDE.md)** — a step-by-step tutorial for junior developers covering everything from environment setup to production release.

---

## 🚀 Getting Started

> 🧙 **Quick Start with [GreenWizard](https://kmp.libstudio.my.id)**
> Generate this template with your custom **project name** and **package ID** — no manual renaming needed. Just enter your details and download the ready-to-go project.

### Prerequisites

- **Android Studio Ladybug** or later (or IntelliJ IDEA with KMP plugin)
- **JDK 17+**
- **Xcode 15+** (for iOS targets)
- **AGP 9.1+** (Internal KMP conventions)

### Clone & Build

```bash
git clone https://github.com/firdaus1453/TemplateKMP.git
cd TemplateKMP
./gradlew assemble
```

### Run via CLI

<details>
<summary><b>📱 Android</b></summary>

```bash
# 1. Build debug APK
./gradlew :androidApp:assembleDebug

# 2. Install to connected device/emulator
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk

# 3. Launch the app
adb shell am start -n com.template.project/.MainActivity
```

> **Prerequisites:**
> - Emulator running (`adb devices` to verify) or physical device connected with USB debugging enabled.
> - `adb` available in PATH (included with Android SDK `platform-tools`).

</details>

<details>
<summary><b>🖥️ Desktop (JVM)</b></summary>

```bash
# Run directly (opens desktop window)
./gradlew :composeApp:run
```

To create a native distributable (installer):

```bash
# macOS → .dmg | Windows → .msi | Linux → .deb
./gradlew :composeApp:createDistributable
```

> Output location: `composeApp/build/compose/binaries/`

</details>

<details>
<summary><b>🍎 iOS (macOS only)</b></summary>

**Option 1 — Xcode (Recommended):**

Open `iosApp/iosApp.xcodeproj` in Xcode, select a simulator, and click ▶ Run.

**Option 2 — CLI:**

```bash
# 1. List available simulators (find your device name)
xcrun simctl list devices available

# 2. Boot simulator (if not already running)
xcrun simctl boot "iPhone 16"

# 3. Build the project
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 16e' \
  -configuration Debug build

# 4. Install on booted simulator
xcrun simctl install booted \
  ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphonesimulator/TemplateKmp.app

# 5. Launch
xcrun simctl launch booted com.template.project.TemplateKmp
```

> **Prerequisites:**
> - macOS with **Xcode 15+** and Command Line Tools (`xcode-select --install`).
> - First build will be slow due to Kotlin/Native compilation.
> - If "No matching destination" error appears, check that `IPHONEOS_DEPLOYMENT_TARGET` ≤ your simulator's iOS version.

</details>

---

## 📁 Adding a New Feature Module

1. Add module entries to `settings.gradle.kts`:
   ```kotlin
   include(":feature:yourfeature:domain")
   include(":feature:yourfeature:data")
   include(":feature:yourfeature:presentation")
   ```

2. Create each module with the appropriate convention plugin:
   ```kotlin
   // feature/yourfeature/domain/build.gradle.kts
   plugins {
       alias(libs.plugins.convention.kmp.library)
       alias(libs.plugins.convention.kover)
   }
   
   // feature/yourfeature/data/build.gradle.kts
   plugins {
       alias(libs.plugins.convention.kmp.library)
       alias(libs.plugins.convention.kover)
   }
   
   // feature/yourfeature/presentation/build.gradle.kts
   plugins {
       alias(libs.plugins.convention.cmp.feature)
       alias(libs.plugins.convention.kover)
   }
   ```

3. Follow the MVI pattern:
   - **Domain**: Models + Repository interface (pure Kotlin)
   - **Data**: Repository implementation (Ktor/Room)
   - **Presentation**: ViewModel (State, Action, Event) + Composable Screens

4. Register DI module in `composeApp` and add navigation route

---

## 🧪 Testing

```bash
# Run all tests
./gradlew allTests

# Generate coverage report
./gradlew koverHtmlReport

# Verify coverage threshold
./gradlew koverVerify
```

Each feature module includes:
- **Fake repositories** for unit testing
- **ViewModel tests** using Turbine for Flow assertions (use `Dispatchers.setMain(testDispatcher)` to prevent deadlocks)
- **Kover** integration for code coverage reporting (ensure at least 1 test per module to prevent "No tests discovered")

---

## 📋 Project Conventions

| Convention | Rule |
|-----------|------|
| **Naming** | `Default*Repository`, `*ViewModel`, `*Screen`, `*State`, `*Action`, `*Event` |
| **DI** | One Koin module per feature, registered in `composeApp` |
| **Navigation** | `@Serializable` data objects/classes as routes |
| **Error handling** | `Result<D, E>` sealed interface — never throw exceptions |
| **Logging** | Kermit only — never `println()` or `Log.d()` |
| **Secrets** | `local.properties` + BuildKonfig — never commit API keys |
| **State management** | `SharingStarted.WhileSubscribed(5_000)` for UI state |

---

## 🤝 Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

```
MIT License

Copyright (c) 2025 Muhammad Firdaus

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```