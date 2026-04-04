pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
    }
}

rootProject.name = "LuxAPI"

include("commons", "database", "core")
include("discord")
include("fabric")
include("neoforge")
include("cobblemon")