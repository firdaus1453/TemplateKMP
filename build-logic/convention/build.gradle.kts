plugins {
    `kotlin-dsl`
}

group = "com.template.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.multiplatform.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.androidx.room.gradle.plugin)
    compileOnly(libs.buildkonfig.gradlePlugin)
    compileOnly(libs.buildkonfig.compiler)
    compileOnly(libs.kover.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("cmpApplication") {
            id = "template.cmp.application"
            implementationClass = "CmpApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = "template.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = "template.cmp.library"
            implementationClass = "CmpLibraryConventionPlugin"
        }
        register("cmpFeature") {
            id = "template.cmp.feature"
            implementationClass = "CmpFeatureConventionPlugin"
        }
        register("room") {
            id = "template.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("buildKonfig") {
            id = "template.buildkonfig"
            implementationClass = "BuildKonfigConventionPlugin"
        }
        register("androidApplication") {
            id = "template.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "template.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("kover") {
            id = "template.kover"
            implementationClass = "KoverConventionPlugin"
        }
    }
}
