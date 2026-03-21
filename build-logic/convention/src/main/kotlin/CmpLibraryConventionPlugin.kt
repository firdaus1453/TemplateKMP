import com.template.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension

class CmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("template.kmp.library")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.compose")
            }

            val compose = extensions.getByType(ComposeExtension::class.java).dependencies

            dependencies {
                "commonMainImplementation"(compose.ui)
                "commonMainImplementation"(compose.foundation)
                "commonMainImplementation"(compose.material3)
                "commonMainImplementation"(compose.materialIconsExtended)
                "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}

