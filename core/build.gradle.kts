plugins {
    id("dev.architectury.loom") version "1.9-SNAPSHOT"
}

dependencies {
    implementation(project(":commons"))

    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
}