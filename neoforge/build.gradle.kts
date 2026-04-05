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
            sourceSet(project.sourceSets.test.get())
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

    // --- Unit Testing Framework ---
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // --- Mocking Framework ---
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:2.0.20")

    testImplementation(files(sourceSets.main.get().compileClasspath))
    testRuntimeOnly(files(sourceSets.main.get().runtimeClasspath))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

// FIXED: Upgraded from the deprecated kotlinOptions to the new compilerOptions DSL
tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<ShadowJar> {
    isZip64 = true
    configurations = listOf(shadeFiles)
    archiveClassifier.set("")
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}