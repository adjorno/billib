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

// Frontend modules
include(":frontend:composeApp")
include(":frontend:androidApp")
