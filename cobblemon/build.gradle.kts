plugins {
    kotlin("jvm")
    id("java")
    id("dev.architectury.loom") version "1.9-SNAPSHOT"
}

repositories {
    mavenCentral()
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    compileOnly(project(":commons"))
    compileOnly(project(":core"))
    compileOnly(project(":database"))

    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    /**
     * Cobblemon API
     */
    compileOnly("com.cobblemon:neoforge:1.7.3+1.21.1") {
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

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}