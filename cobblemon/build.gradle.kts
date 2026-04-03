plugins {
    kotlin("jvm")
    id("java")
}

repositories {
    mavenCentral()
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    compileOnly(project(":commons"))
    compileOnly(project(":database"))

    compileOnly("com.cobblemon:cobblemon:1.7.3+1.21.1")
}

kotlin {
    jvmToolchain(21)
}