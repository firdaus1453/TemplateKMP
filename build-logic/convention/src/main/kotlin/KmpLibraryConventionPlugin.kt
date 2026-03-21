import com.android.build.api.dsl.LibraryExtension
import com.template.convention.configureKotlinAndroid
import com.template.convention.configureKotlinMultiplatform
import com.template.convention.libs
import com.template.convention.pathToResourcePrefix
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class KmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureKotlinMultiplatform()

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                resourcePrefix = this@with.pathToResourcePrefix()
            }

            dependencies {
                "commonMainImplementation"(libs.findLibrary("kotlinx-serialization-json").get())
                "commonTestImplementation"(libs.findLibrary("kotlin-test").get())
                "commonTestImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
                "commonTestImplementation"(libs.findLibrary("turbine").get())
            }
        }
    }
}
