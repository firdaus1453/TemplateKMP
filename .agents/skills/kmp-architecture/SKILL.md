---
name: kmp-architecture
description: >
  Clean architecture module boundaries, layer dependencies, and package structure guidelines for Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP) projects following JetBrains' New KMP Default Structure (AGP 9.3+) and Chirp best practices. Trigger when: creating new feature modules, configuring Gradle dependencies, designing UseCase / business logic classes, or establishing boundaries between presentation, domain, and data layers.
---

# KMP Multi-Module Architecture & Chirp Clean Architecture

This document defines the Gradle module layout, package boundary rules, dependency conventions, and convention plugins used by this Kotlin Multiplatform project.

---

## 🧱 Core Philosophy

- **Feature-Grouped Bounded Contexts**: The project is modularized by **user journey** (e.g., `:feature:auth`, `:feature:home`, `:feature:profile`, `:feature:settings`), not by one giant monolithic module.
- **3-Layer Split per Feature**: Within each feature module, responsibilities are cleanly divided into:
  - **`domain`**: Pure Kotlin models, repository interfaces, validators, use cases (**ZERO framework dependencies**).
  - **`data`**: Implements repository and data source interfaces using Ktor 3.x, Room KMP, and DataStore.
  - **`presentation`**: MVI ViewModels (`koinViewModel`) + Compose Multiplatform UI screens.
- **Core Isolation**: Shared utilities, design system tokens, base network factories (`HttpClientFactory`), and shared data models live under `:core:*`. Features must **never** depend on other feature modules.

---

## 📂 JetBrains New KMP Default Structure Layout

Aligned with the **JetBrains New KMP Default Project Structure** and **AGP 9.3**:

```
project/
├── build-logic/
│   └── convention/                   # Gradle Convention Plugins (AGP 9.3 + CMP 1.12)
├── androidApp/                       # Standalone Android Application Entry (`com.android.application` + built-in Kotlin)
├── composeApp/                       # Composition root & Shared UI Library (Koin Start, NavHost, Desktop/iOS Entry)
├── core/                             # Shared libraries (com.android.kotlin.multiplatform.library)
│   ├── domain/                       # Pure Kotlin: Result<D, E>, RootError, DataError, interfaces
│   ├── data/                         # Ktor HttpClientFactory, SessionStorage, safeCall, auth
│   ├── presentation/                 # UiText, ObserveAsEvents, shared UI helpers
│   └── designsystem/                 # AppTheme, Colors, Typography, Shapes, atomic UI components
├── feature/
│   ├── auth/                         # Feature: domain, presentation
│   ├── home/                         # Feature: domain, data, presentation
│   ├── search/                       # Feature: domain, data, presentation
│   ├── profile/                      # Feature: domain, data, presentation
│   └── settings/                     # Feature: domain, data, presentation
├── .maestro/                         # Automated headless Maestro UI test flows for AI / CI
├── scripts/                          # Automated test runner scripts (run_maestro_tests.sh)
├── gradle/libs.versions.toml         # Version catalog
└── settings.gradle.kts               # Module includes
```

---

## ⛔ Package Boundary & Dependency Rules

```
androidApp             ──► composeApp
composeApp             ──► core/* + feature/*/presentation + feature/*/data
feature/*/presentation ──► feature/*/domain + core/presentation + core/designsystem
feature/*/data         ──► feature/*/domain + core/domain + core/data
core/data              ──► core/domain
core/presentation      ──► core/domain + core/designsystem

🚫 feature.presentation ✕ feature.data  (NEVER import data directly into presentation)
🚫 feature.X            ✕ feature.Y     (NEVER cross-import between features)
```

### Layer Constraints (Chirp Discipline)
1. **Domain Layer (`*.domain.*`)**:
   - Pure Kotlin — **ZERO** Android/JVM/iOS framework dependencies (no `android.*`, no `androidx.compose.*`, no Ktor/Room imports).
   - Contains: domain models, repository interfaces, use cases, business validation rules.
2. **Data Layer (`*.data.*`)**:
   - Implements repository interfaces defined in `domain`.
   - Contains: `datasource/` (local Room + remote Ktor implementations), `repository/`, `dto/` (API serialization models), `mapper/` (DTO/Entity ↔ Domain models).
   - May depend on: `domain`, `core:domain`, `core:data`.
3. **Presentation Layer (`*.presentation.*`)**:
   - Injects repository / use case interfaces from `domain`.
   - Contains: ViewModels (MVI), Compose screens, UI state models, UI event handlers.
   - May depend on: `domain`, `core:domain`, `core:presentation`, `core:designsystem`.

---

## 🛠️ Convention Plugins (`build-logic/convention`)

| Plugin ID | Target | Purpose |
|---|---|---|
| `template.kmp.library` | Core/feature domain/data | Pure KMP library module (`com.android.kotlin.multiplatform.library`) |
| `template.cmp.library` | Core UI modules (`core:designsystem`) | KMP library + Compose Multiplatform |
| `template.cmp.feature` | Feature presentation modules | Compose + ViewModel + Koin + MVI |
| `template.cmp.application` | `:composeApp` | Main shared application composition root |
| `template.android.application` | `:androidApp` | Standalone Android application entry (AGP 9.3) |
| `template.android.application.compose` | `:androidApp` | Android application with Compose integration |
| `template.room` | Room modules | Room Multiplatform config + KSP |
| `template.kover` | All testable modules | Kover code coverage aggregation |
| `template.buildkonfig` | `:core:data` | Build-time constants from `local.properties` |
