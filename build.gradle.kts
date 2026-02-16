plugins {
    // Backend plugins
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    jacoco
    application

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
}

application {
    mainClass.set("com.adjorno.billib.rest.BBRestApplicationKt")
}

group = "com.adjorno.billib.rest"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.spring.boot.starter)
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.gson)
    implementation(libs.postgresql)
    implementation(libs.jsoup)
    implementation(libs.m14n.ex)
    implementation(libs.m14n.billib.data)
    implementation(libs.jakarta.xml.bind.api)
    implementation(libs.glassfish.jaxb.runtime)
}

tasks.named<Jar>("jar") {
    enabled = false
}

// Configure frontend modules
subprojects {
    if (project.path.startsWith(":frontend")) {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "io.gitlab.arturbosch.detekt")

        plugins.withId("org.jlleitschuh.gradle.ktlint") {
            configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
                debug.set(true)
                filter {
                    exclude { it.file.path.contains("/build/") }
                }
            }
        }

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

                    // Default URL for desktop/WASM development
                    buildConfigField(
                        com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                        "API_BASE_URL",
                        apiBaseUrl ?: "http://localhost:8080"
                    )
                }

                // Android-specific: Use 10.0.2.2 for emulator (emulator's host loopback)
                targetConfigs {
                    create("android") {
                        buildConfigField(
                            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                            "API_BASE_URL",
                            apiBaseUrl ?: "http://10.0.2.2:8080"
                        )
                    }
                }
            }
        }
    }
}
