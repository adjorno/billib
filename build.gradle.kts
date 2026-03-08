plugins {
    // Backend plugins
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false

    // Frontend plugins (apply false - will be applied in frontend modules)
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.multiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.gmsGoogleServices) apply false
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
                packageName = "com.ifochka.m14n"
                val useFirebaseEmulator = getPropertyOrEnv(key = "USE_FIREBASE_EMULATOR", fallback = "false")
                val apiBaseUrl = getPropertyOrEnv(
                    key = "API_BASE_URL",
                    fallback = if (useFirebaseEmulator == "true") "http://localhost:8080" else "https://api.m14n.com",
                )

                val versionName = getPropertyOrEnv(key = "VERSION_NAME", fallback = "local build")
                val apiKey = getPropertyOrEnv(key = "FIREBASE_API_KEY", fallback = "local-api-key")
                val appleMusicToken = getPropertyOrEnv(key = "APPLE_MUSIC_TOKEN", fallback = "")
                val firebaseProjectId = getPropertyOrEnv(key = "FIREBASE_PROJECT_ID", fallback = "m14n-41a5a")
                val firebaseAppId = getPropertyOrEnv(key = "FIREBASE_APP_ID", fallback = "")
                val firebaseAuthDomain = getPropertyOrEnv(
                    key = "FIREBASE_AUTH_DOMAIN",
                    fallback = "$firebaseProjectId.firebaseapp.com",
                )
                val googleWebClientId = getPropertyOrEnv(key = "GOOGLE_WEB_CLIENT_ID", fallback = "")
                val appleClientId = getPropertyOrEnv(key = "APPLE_CLIENT_ID", fallback = "")

                defaultConfigs {
                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "VERSION_NAME",
                        value = versionName,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "API_BASE_URL",
                        value = apiBaseUrl,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "FIREBASE_API_KEY",
                        value = apiKey,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "APPLE_MUSIC_TOKEN",
                        value = appleMusicToken,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "FIREBASE_PROJECT_ID",
                        value = firebaseProjectId,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "FIREBASE_APP_ID",
                        value = firebaseAppId,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "FIREBASE_AUTH_DOMAIN",
                        value = firebaseAuthDomain,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
                        name = "USE_FIREBASE_EMULATOR",
                        value = useFirebaseEmulator,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "GOOGLE_WEB_CLIENT_ID",
                        value = googleWebClientId,
                    )

                    buildConfigField(
                        type = com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        name = "APPLE_CLIENT_ID",
                        value = appleClientId,
                    )
                }
            }
        }
    }
}

fun Project.getPropertyOrEnv(key: String, fallback: String): String {
    return System.getenv(key)
        ?: findProperty(key)?.toString()
        ?: fallback
}
