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
    compileOnly(project(":database"))

    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    /**
     * Cobblemon API
     */
    compileOnly("com.cobblemon:neoforge:1.7.3+1.21.1") {
        isTransitive = false
    }
}

kotlin {
    jvmToolchain(21)
}