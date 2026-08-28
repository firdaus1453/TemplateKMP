package com.template.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<KotlinMultiplatformExtension> {
        iosArm64()
        iosSimulatorArm64()

        jvm("desktop")

        // Android target is auto-created by com.android.kotlin.multiplatform.library
        // Configure it via 'androidLibrary' extension on KotlinMultiplatformExtension
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = libs.findVersion("projectApplicationId").get().toString() +
                this@configureKotlinMultiplatform.path.removePrefix(":").replace(":", ".").let { if (it.isNotEmpty()) ".$it" else "" }
            compileSdk = libs.findVersion("projectCompileSdkVersion").get().toString().toInt()
            minSdk = libs.findVersion("projectMinSdkVersion").get().toString().toInt()
        }
    }
}
