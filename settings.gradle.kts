pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
    }
}

rootProject.name = "LuxAPI"

include("commons", "database")
include("discord")
include("fabric")
include("neoforge")
include("cobblemon")