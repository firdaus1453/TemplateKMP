rootProject.name = "TemplateKmp"
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

// Feature: Auth
include(":feature:auth:domain")
include(":feature:auth:presentation")

// Feature: Home
include(":feature:home:domain")
include(":feature:home:data")
include(":feature:home:presentation")

// Feature: Profile
include(":feature:profile:domain")
include(":feature:profile:data")
include(":feature:profile:presentation")

// Feature: Settings
include(":feature:settings:domain")
include(":feature:settings:data")
include(":feature:settings:presentation")

// Feature: Search
include(":feature:search:domain")
include(":feature:search:data")
include(":feature:search:presentation")

// Feature: Notifications (stub)
include(":feature:notifications:domain")
include(":feature:notifications:presentation")

// Feature: Media (stub)
include(":feature:media:domain")
include(":feature:media:presentation")