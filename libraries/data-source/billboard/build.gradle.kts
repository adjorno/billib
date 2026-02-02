plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    application
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