import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.multiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(21) // Match backend JVM version

    androidLibrary {
        namespace = "com.ifochka.billib"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        androidResources.enable = true
    }

    jvm() // Desktop

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.startup.runtime)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kamel.image.default)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
            implementation(libs.sqldelight.sqlite.driver)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.sqldelight.web.worker.driver)
            implementation(npm("sql.js", libs.versions.sqljs.get()))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
            implementation(devNpm("copy-webpack-plugin", libs.versions.webpack.get()))
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.ifochka.billib.MainKt"

        nativeDistributions {
            packageName = "M14N"
            packageVersion = "1.0.0"

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            macOS {
                bundleID = "com.ifochka.billib"
                iconFile.set(project.file("icons/icon.icns"))
                appCategory = "public.app-category.utilities"
                minimumSystemVersion = "12.0"
            }

            windows {
                iconFile.set(project.file("icons/icon.ico"))
            }

            linux {
                iconFile.set(project.file("icons/icon.png"))
            }
        }
    }
}

sqldelight {
    databases {
        create("BillibDatabase") {
            packageName.set("com.ifochka.billib.db")
            generateAsync.set(true)
        }
    }
}
