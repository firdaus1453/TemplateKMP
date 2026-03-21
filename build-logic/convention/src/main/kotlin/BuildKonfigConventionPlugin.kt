import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.template.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Properties

class BuildKonfigConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.codingfeline.buildkonfig")
            }

            extensions.configure<BuildKonfigExtension> {
                packageName = libs.findVersion("projectApplicationId").get().toString()

                val localProperties = Properties()
                val localPropertiesFile = rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    localPropertiesFile.inputStream().use { localProperties.load(it) }
                }

                defaultConfigs {
                    buildConfigField(
                        com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        "API_KEY",
                        localProperties.getProperty("API_KEY") ?: "",
                    )
                    buildConfigField(
                        com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        "BASE_URL",
                        localProperties.getProperty("BASE_URL") ?: "https://dummyjson.com",
                    )
                }
            }
        }
    }
}

