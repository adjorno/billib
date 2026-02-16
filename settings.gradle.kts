rootProject.name = "BilliBRESTJava"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Existing backend modules
include("libraries:data-source:billboard")
project(":libraries:data-source:billboard").name = "billboard"
include("libraries:inmemory-rest")
include("billib-importer")

// Railway deployment skips frontend (doesn't have Android SDK)
val isRailwayBuild = System.getenv("RAILWAY_ENVIRONMENT") != null
if (!isRailwayBuild) {
    include(":frontend:composeApp")
    include(":frontend:androidApp")
} else {
    logger.lifecycle("Frontend modules excluded (Railway environment detected)")
}
