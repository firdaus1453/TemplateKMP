plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.buildkonfig)
    alias(libs.plugins.convention.kover)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.auth.domain)

            implementation(libs.bundles.ktor.common)
            implementation(libs.datastore.preferences)
            implementation(libs.touchlab.kermit)
            implementation(libs.kotlinx.datetime)

            api(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

// Chucker HTTP inspector — debug only, no-op in release
dependencies {
    "debugImplementation"(libs.chucker.debug)
    "releaseImplementation"(libs.chucker.release)
}
