import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("dev.architectury.loom") version "1.9-SNAPSHOT"
}

base {
    archivesName.set("LuxAPI-Fabric-1.21.1")
}

val shadeFiles by configurations.creating

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.16.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.0+1.21.1")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.1+kotlin.2.0.20")

    implementation(project(":commons"))
    implementation(project(":core"))
    implementation(project(":cobblemon"))

    shadeFiles(project(":commons")) {
        isTransitive = false
    }

    // --- Unit Testing Framework ---
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // --- Mocking Framework ---
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:2.0.20")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.withType<ShadowJar> {
    isZip64 = true
    configurations = listOf(shadeFiles)
    archiveClassifier.set("dev-shadow")
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    dependsOn(tasks.named("shadowJar"))

    val shadowTask = tasks.named<ShadowJar>("shadowJar")
    inputFile.set(shadowTask.flatMap { it.archiveFile })
}

java {
    withSourcesJar()
}

apply(plugin = "maven-publish")

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.novaco.luxapi"
            artifactId = "fabric"
            version = "1.0.0"

            artifact(tasks.named("remapJar"))
        }
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}