plugins {
    id("dev.architectury.loom") version "1.9-SNAPSHOT"
}

dependencies {
    implementation(project(":commons"))

    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    // --- Unit Testing Framework ---
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // --- Mocking Framework ---
    testImplementation("org.mockito:mockito-core:5.11.0")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}