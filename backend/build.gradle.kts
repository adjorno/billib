plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    jacoco
    application
}

application {
    mainClass.set("com.ifochka.billib.rest.BBRestApplicationKt")
}

group = "com.ifochka.billib.rest"
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

    // Project dependencies
    implementation(project(":libraries:data-source:billboard"))
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveClassifier.set("")
    archiveBaseName.set("BilliBRESTJava")
}
