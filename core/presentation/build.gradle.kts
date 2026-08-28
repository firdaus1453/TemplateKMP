plugins {
    alias(libs.plugins.convention.cmp.library)
    alias(libs.plugins.convention.kover)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)

            implementation(libs.jetbrains.lifecycle.compose)
            implementation(libs.jetbrains.lifecycle.viewmodel)
            implementation(compose.runtime)
        }
    }
}
