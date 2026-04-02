pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "LuxAPI"

// Core & Database
include("commons", "sqlite")

// Integrations
include("discord")

// Platforms (Cobblemon Focus)
include("fabric")
include("neoforge")