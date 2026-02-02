plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    jacoco
    application
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
