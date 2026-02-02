plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    application
}

group = "com.m14n.billib"
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
    implementation(libs.rome)
    implementation(libs.jsoup)
    implementation(libs.httpclient)
    implementation(libs.m14n.ex)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
}

application {
    // Default to consistency checker, but can be overridden
    mainClass.set("com.m14n.billib.data.billboard.html.RefetchInconsistentChartsKt")
}

tasks.register<JavaExec>("refetch") {
    group = "billboard"
    description = "Refetch inconsistent charts from Billboard"
    mainClass.set("com.m14n.billib.data.billboard.html.RefetchInconsistentChartsKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("check-consistency") {
    group = "billboard"
    description = "Check data consistency"
    mainClass.set("com.m14n.billib.data.billboard.BBChartDataCheckerKt")
    classpath = sourceSets["main"].runtimeClasspath
}
