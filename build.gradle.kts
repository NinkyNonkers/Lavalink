import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {
    group = "lavalink"

    repositories {
        mavenCentral()
        google()
        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")
        maven("https://jitpack.io") // build projects directly from GitHub
        maven ("https://maven.aliyun.com/repository/jcenter")
        maven("https://m2.dv8tion.net/releases")
    }
}

plugins {
    id("org.jetbrains.dokka") version "1.8.20" apply false
    id("org.springframework.boot") version "3.1.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "idea")

    if (project.hasProperty("includeAnalysis")) {
        project.logger.lifecycle("applying analysis plugins")
        apply(from = "../analysis.gradle")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }

}