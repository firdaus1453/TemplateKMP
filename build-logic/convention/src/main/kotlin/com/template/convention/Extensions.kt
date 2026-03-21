package com.template.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.pathToResourcePrefix(): String {
    return path
        .removePrefix(":")
        .replace(":", "_")
        .replace("-", "_")
}
