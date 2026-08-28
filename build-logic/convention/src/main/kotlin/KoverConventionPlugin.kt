import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            
            // WORKAROUND for AGP 9 com.android.kotlin.multiplatform.library vs Kover 0.9.1
            // Kover expects an "android" extension with variants, else it crashes.
            // By supplying an empty list of variants, Kover skips Android instrumentation 
            // but still runs successfully for KMP standard targets (like desktop/JVM).
            if (extensions.findByName("android") == null) {
                extensions.add("android", object {
                    val libraryVariants: Collection<Any> = emptyList()
                    val applicationVariants: Collection<Any> = emptyList()
                    val unitTestVariants: Collection<Any> = emptyList()
                    val testVariants: Collection<Any> = emptyList()
                    val buildTypes: Collection<Any> = emptyList()
                    val productFlavors: Collection<Any> = emptyList()
                    val defaultConfig = object {
                        val missingDimensionStrategies: Map<String, Any> = emptyMap()
                    }
                })
            }

            pluginManager.apply("org.jetbrains.kotlinx.kover")

            extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
                reports {
                    filters {
                        excludes {
                            classes(
                                "*_Factory*",
                                "*_HiltModules*",
                                "*BuildKonfig*",
                                "*ComposableSingletons*",
                                "*ScreenKt*",
                                "*Screen$*",
                                "*Screen",
                                "*ModuleKt*",
                                "*Module",
                                "*_Impl*",
                                "*\$serializer*",
                                "*ObserveAsEventsKt*",
                                "*PlatformDataStoreModule*",
                                "*PlatformEngine*",
                                "*HttpClientFactory*",
                                "*DataStoreFactory*",
                                "*DataStorePlatform*",
                                "*AppThemeKt*",
                                "*AppKt*",
                            )
                            packages(
                                "com.template.project.core.data.di",
                                "com.template.project.core.designsystem",
                                "com.template.project.core.designsystem.components",
                                "com.template.project.feature.auth.presentation.di",
                                "com.template.project.feature.home.data.di",
                                "com.template.project.feature.home.presentation.di",
                                "com.template.project.feature.profile.data.di",
                                "com.template.project.feature.profile.presentation.di",
                                "com.template.project.feature.search.data.di",
                                "com.template.project.feature.search.presentation.di",
                                "com.template.project.feature.settings.data.di",
                                "com.template.project.feature.settings.presentation.di",
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
