import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("net.neoforged.moddev") version "1.0.17"
    id("com.gradleup.shadow")
}

base {
    archivesName.set("LuxAPI-NeoForge-1.21.1")
}

val shadeFiles by configurations.creating

neoForge {
    version = "21.1.30"

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
        }
        create("client") {
            client()
        }
        create("server") {
            server()
        }
    }

    mods {
        create("luxapi") {
            sourceSet(project.sourceSets.main.get())
        }
    }
}

repositories {
    maven("https://thedarkcolour.github.io/KotlinForForge/") {
        name = "KotlinForForge"
    }
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":core"))
    implementation(project(":cobblemon"))
    implementation("thedarkcolour:kotlinforforge-neoforge:5.0.1")

    shadeFiles(project(":commons")) {
        isTransitive = false
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.withType<ShadowJar> {
    isZip64 = true
    configurations = listOf(shadeFiles)

    archiveClassifier.set("")
}

java {
    withSourcesJar()
}