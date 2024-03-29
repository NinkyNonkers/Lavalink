import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.dokka") version "1.8.20" apply false
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("org.ajoberstar.grgit") version "5.2.0"
    id("org.springframework.boot") version "3.1.0" apply false
    id("org.sonarqube") version "4.2.0.3129"
    id("com.adarshr.test-logger") version "3.2.0"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22" apply false
}


allprojects {
    group = "lavalink"

    repositories {
        mavenCentral() // main maven repo
        mavenLocal()   // useful for developing
        maven("https://m2.dv8tion.net/releases")
        maven("https://jitpack.io") // build projects directly from GitHub
        maven("https://plugins.gradle.org/m2/")
        maven("https://repo.spring.io/plugins-release")
        maven("https://maven.lavalink.dev/releases")
    }
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
