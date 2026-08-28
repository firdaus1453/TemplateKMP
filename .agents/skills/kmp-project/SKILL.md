---
name: kmp-project
description: >
  Step-by-step workflow for creating new feature modules in KMP/CMP adhering to the New KMP Default Structure and Chirp Clean Architecture. Trigger when: the user asks to "add a feature", "create a new module", "scaffold a screen", or configure settings.gradle.kts.
---

# Adding a New Feature Module (Step-by-Step)

Follow this strict checklist whenever creating a new feature module in this project:

---

## 1. Register Modules in `settings.gradle.kts`
```kotlin
include(":feature:bookmark:domain")
include(":feature:bookmark:data")
include(":feature:bookmark:presentation")
```

---

## 2. Create `build.gradle.kts` for Each Sub-Module

### A. `feature/bookmark/domain/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kover)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}
```

### B. `feature/bookmark/data/build.gradle.kts`
```kotlin
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
        }
    }
}
```

### C. `feature/bookmark/presentation/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.convention.cmp.feature)
    alias(libs.plugins.convention.kover)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.presentation)
            implementation(projects.core.designsystem)
            implementation(projects.feature.bookmark.domain)
        }
    }
}
```

---

## 3. Implement Clean Architecture & MVI Components

1. **Domain**: Define models and `BookmarkRepository` interface.
2. **Data**: Implement `DefaultBookmarkRepository` using Ktor/Room.
3. **Presentation**: Create `BookmarkState`, `BookmarkAction`, `BookmarkEvent`, `BookmarkViewModel`, and `BookmarkScreen`.
4. **DI**: Define `bookmarkModule` and register it in `composeApp`'s `initKoin()`.
5. **Navigation**: Add `@Serializable data object BookmarkRoute` to `NavHost` in `App.kt`.
6. **Tests**: Add unit test for ViewModel with Turbine in `commonTest`.
