plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":commons"))

    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "description" to project.description
        )

        inputs.properties(props)

        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveBaseName.set("LuxAPI-Bukkit")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
    }

    jar {
        archiveBaseName.set("LuxAPI-Bukkit")
    }
}