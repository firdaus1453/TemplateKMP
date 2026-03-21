plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.kover)
}

dependencies {
    // Kover aggregation — all testable modules
    kover(projects.core.domain)
    kover(projects.core.data)
    kover(projects.feature.auth.domain)
    kover(projects.feature.auth.presentation)
    kover(projects.feature.home.domain)
    kover(projects.feature.home.data)
    kover(projects.feature.home.presentation)
    kover(projects.feature.profile.domain)
    kover(projects.feature.profile.data)
    kover(projects.feature.profile.presentation)
    kover(projects.feature.settings.domain)
    kover(projects.feature.settings.data)
    kover(projects.feature.settings.presentation)
    kover(projects.feature.search.domain)
    kover(projects.feature.search.data)
    kover(projects.feature.search.presentation)
    kover(projects.feature.media.domain)
    kover(projects.feature.media.presentation)
    kover(projects.feature.notifications.domain)
    kover(projects.feature.notifications.presentation)
}