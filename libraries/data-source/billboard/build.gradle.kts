plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.detekt)
    application
}

group = "com.m14n"
version = "0.0.6"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)
}

application {
    // Default to consistency checker, but can be overridden
    mainClass.set("com.m14n.data.billboard.html.RefetchInconsistentChartsKt")
}

tasks.register<JavaExec>("refetch") {
    group = "billboard"
    description = "Refetch inconsistent charts from Billboard"
    mainClass.set("com.m14n.data.billboard.html.RefetchInconsistentChartsKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("check-consistency") {
    group = "billboard"
    description = "Check data consistency"
    mainClass.set("com.m14n.data.billboard.BBChartDataCheckerKt")
    classpath = sourceSets["main"].runtimeClasspath
}
