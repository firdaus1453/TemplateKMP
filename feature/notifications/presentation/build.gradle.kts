plugins {
    alias(libs.plugins.convention.cmp.feature)
    alias(libs.plugins.convention.kover)
}
kotlin { sourceSets { commonMain.dependencies { implementation(projects.feature.notifications.domain) } } }
