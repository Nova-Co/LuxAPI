plugins {
    kotlin("jvm")
    id("java")
    id("dev.architectury.loom") version "1.9-SNAPSHOT"
}

repositories {
    mavenCentral()
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    compileOnly(project(":commons"))
    compileOnly(project(":core"))
    compileOnly(project(":database"))

    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    /**
     * Cobblemon API
     */
    modCompileOnly("com.cobblemon:mod:1.7.3+1.21.1") {
        isTransitive = false
    }

    // Cobblemon's own ActionEffectTimeline/MoLangRuntime classes reference com.bedrockk.molang.*
    // directly in their public signatures. Since `mod` is isTransitive = false (see above), those
    // classes aren't otherwise on the compile classpath, which produces a "may be forbidden soon"
    // compiler warning wherever LuxAPI code touches them (see MoLangCinematicAPI.kt). compileOnly
    // here silences that cleanly: the real classes are supplied by Cobblemon's own jar at runtime,
    // so nothing ships or changes at runtime — this only satisfies the compiler.
    compileOnly("com.bedrockk:molang:1.1.20")

    // --- Unit Testing Framework ---
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // --- Mocking Framework ---
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:2.0.20")

    // Cobblemon's `mod` artifact is deliberately non-transitive (isTransitive = false, see above),
    // so MoLang isn't on the compile classpath. That's fine for compilation, but merely loading
    // com.cobblemon.mod.common.pokemon.Pokemon at test runtime (e.g. to mock it) triggers its
    // MoLang-backed `struct` field init, which needs the real classes. Test-scope only — does not
    // affect the shipped mod jar's dependency graph.
    testImplementation("com.bedrockk:molang:1.1.20")

    testImplementation(files(sourceSets.main.get().compileClasspath))
    testRuntimeOnly(files(sourceSets.main.get().runtimeClasspath))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}