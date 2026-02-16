import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.ifochka.billib.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ifochka.billib"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    // Try keystore.properties first (local dev), fallback to env vars (CI/CD)
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    val useKeystoreFile = keystorePropertiesFile.exists()

    if (useKeystoreFile) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    // Determine keystore path from either source
    val keystorePath = if (useKeystoreFile) {
        keystoreProperties["storeFile"]?.toString()
    } else {
        System.getenv("ANDROID_KEYSTORE_PATH")
    }

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = if (useKeystoreFile) {
                    keystoreProperties["storePassword"]?.toString()
                } else {
                    System.getenv("ANDROID_KEYSTORE_PASSWORD")
                }
                keyAlias = if (useKeystoreFile) {
                    keystoreProperties["keyAlias"]?.toString()
                } else {
                    System.getenv("ANDROID_KEY_ALIAS")
                }
                keyPassword = if (useKeystoreFile) {
                    keystoreProperties["keyPassword"]?.toString()
                } else {
                    System.getenv("ANDROID_KEY_PASSWORD")
                }
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    implementation(projects.frontend.composeApp)
    implementation(libs.androidx.activity.compose)
}
