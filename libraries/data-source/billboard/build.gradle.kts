plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
}

group = "com.m14n.billib"
version = "0.0.6"

repositories {
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(libs.rome)
    implementation(libs.jsoup)
    implementation(libs.httpclient)
    implementation(libs.ex)
    implementation(libs.kotlinStdlib)
    implementation(libs.kotlinSerializationJson)

    testImplementation(libs.junit)
    testImplementation(libs.mockitoKotlin)
}