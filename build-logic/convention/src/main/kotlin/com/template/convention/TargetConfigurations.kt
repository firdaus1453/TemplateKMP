package com.template.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureIosTargets() {
    extensions.configure<KotlinMultiplatformExtension> {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach {
            it.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
                freeCompilerArgs += listOf(
                    "-Xbinary=bundleId=com.template.project.ComposeApp"
                )
            }
        }
    }
}

internal fun Project.configureDesktopTarget() {
    extensions.configure<KotlinMultiplatformExtension> {
        jvm("desktop")
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun KotlinMultiplatformExtension.applyHierarchyTemplate() {
    applyDefaultHierarchyTemplate {
        common {
            group("mobile") {
                withAndroidTarget()
                withIosArm64()
                withIosSimulatorArm64()
            }
            group("nonMobile") {
                withJvm()
            }
        }
    }
}
