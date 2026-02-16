plugins {
    // Backend plugins
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false

    // Frontend plugins (apply false - will be applied in frontend modules)
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.multiplatformLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// Configure all subprojects with ktlint
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            debug.set(true)
            filter {
                exclude { it.file.path.contains("/build/") }
            }
        }
    }

    // Configure frontend-specific tools and configuration
    if (project.path.startsWith(":frontend")) {
        apply(plugin = "io.gitlab.arturbosch.detekt")

        plugins.withId("io.gitlab.arturbosch.detekt") {
            configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
                buildUponDefaultConfig = true
                config.setFrom(files("$rootDir/detekt.yml"))
            }
            tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
                exclude { it.file.path.contains("/build/") }
            }
        }

        plugins.withId("com.codingfeline.buildkonfig") {
            configure<com.codingfeline.buildkonfig.gradle.BuildKonfigExtension> {
                packageName = "com.ifochka.billib"
                val apiBaseUrl = System.getenv("API_BASE_URL")

                defaultConfigs {
                    buildConfigField(
                        com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        "VERSION_NAME",
                        "1.0.0"
                    )

                    buildConfigField(
                        com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        "API_BASE_URL",
                        apiBaseUrl ?: "http://localhost:8080"
                    )
                }

            }
        }
    }
}
