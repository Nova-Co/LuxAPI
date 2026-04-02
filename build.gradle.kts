import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    group = "com.lux.api"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

subprojects {
    pluginManager.apply("org.jetbrains.kotlin.jvm")
    pluginManager.apply("maven-publish")

    dependencies {
        add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }
}