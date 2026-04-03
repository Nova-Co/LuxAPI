plugins {
    kotlin("jvm")
    id("java")
    id("fabric-loom") version "1.7-SNAPSHOT"
}

repositories {
    mavenCentral()
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://api.modrinth.com/maven")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    compileOnly(project(":commons"))
    compileOnly(project(":database"))

    /**
     * Minecraft Server Dependency
     */
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    /**
     * Cobblemon API
     */
    modCompileOnly("com.cobblemon:mod:1.7.3+1.21.1")
}

kotlin {
    jvmToolchain(21)
}