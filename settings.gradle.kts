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
include("fabric")
include("neoforge")
include("cobblemon")
include("economy")
include("bukkit")
include("discord")