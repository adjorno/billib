import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.multiplatformLibrary)
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.kotlin.native.cocoapods")
}

kotlin {
    jvmToolchain(21)

    androidLibrary {
        namespace = "com.ifochka.auth"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "ifochka Firebase auth library"
        homepage = "https://ifochka.com"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "IfochkaAuth"
            isStatic = true
        }
        pod("GoogleSignIn") { version = "~> 8.0" }
    }

    sourceSets {
        // Intermediate source set for Firebase-capable targets: Android, JVM desktop, iOS.
        // wasmJs has no firebase-auth artifact so it gets a separate implementation.
        val firebaseMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.firebase.auth.multiplatform)
            }
        }
        androidMain { dependsOn(firebaseMain) }
        jvmMain { dependsOn(firebaseMain) }
        iosMain { dependsOn(firebaseMain) }
        // Wire iOS target compilations to iosMain — required because the default hierarchy
        // template is not applied when custom dependsOn edges exist (firebaseMain).
        getByName("iosArm64Main") { dependsOn(iosMain.get()) }
        getByName("iosSimulatorArm64Main") { dependsOn(iosMain.get()) }

        commonMain.dependencies {
            implementation(projects.frontend.ifochkaCore)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.google.identity.googleid)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

// Firebase BOM must be declared at top-level for Android platform resolution in KMP library modules
dependencies {
    add("androidMainImplementation", platform(libs.firebase.bom))
}
