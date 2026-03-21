import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")

            extensions.configure<KoverProjectExtension> {
                reports {
                    filters {
                        excludes {
                            classes(
                                "*_Factory",
                                "*_HiltModules*",
                                "*BuildKonfig*",
                                "*ComposableSingletons*",
                                "*.di.*Module*",
                                "*_Impl",
                                "*_Impl\$*",
                            )
                            packages(
                                "*.di",
                                "*.theme",
                                "*.designsystem",
                            )
                        }
                    }

                    verify {
                        rule("Minimum coverage") {
                            minBound(0)
                        }
                    }
                }
            }
        }
    }
}
