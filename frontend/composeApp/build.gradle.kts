import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.multiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(21) // Match backend JVM version

    androidLibrary {
        namespace = "com.ifochka.m14n"
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
            implementation(libs.compose.ui.tooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.startup.runtime)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.firebase.auth.multiplatform)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.androidx.nav3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.androidx.adaptive)
            implementation(libs.androidx.adaptive.layout)
            implementation(libs.androidx.adaptive.nav3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.compose.ui.tooling)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.firebase.auth.multiplatform)
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
        mainClass = "com.ifochka.m14n.MainKt"

        nativeDistributions {
            packageName = "M14N"
            packageVersion = "1.0.0"

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            macOS {
                bundleID = "com.ifochka.m14n"
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

// Firebase BOM must be declared at top-level for Android platform resolution in KMP library modules
dependencies {
    add("androidMainImplementation", platform(libs.firebase.bom))
}

sqldelight {
    databases {
        create("M14nDatabase") {
            packageName.set("com.ifochka.m14n.db")
            generateAsync.set(true)
        }
    }
}
