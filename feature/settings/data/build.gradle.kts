plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kover)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.settings.domain)
            implementation(libs.datastore.preferences)
            implementation(libs.koin.core)
        }
    }
}
