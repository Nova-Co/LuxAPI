import org.gradle.api.component.AdhocComponentWithVariants
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.code.gson:gson:2.11.0")
        classpath("org.jetbrains.kotlin:kotlin-metadata-jvm:2.2.0")
    }
}

plugins {
    kotlin("jvm") version "2.1.10" apply false
    id("com.gradleup.shadow") version "8.3.6" apply false
    id("com.vanniktech.maven.publish") version "0.32.0" apply false
}

allprojects {
    group = "io.github.nova-co"
    version = "1.10.1"

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.spongepowered.org/maven")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "com.gradleup.shadow")

    afterEvaluate {
        (components["java"] as AdhocComponentWithVariants).withVariantsFromConfiguration(
            configurations["shadowRuntimeElements"]
        ) { skip() }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        configure(
            com.vanniktech.maven.publish.KotlinJvm(
                javadocJar = com.vanniktech.maven.publish.JavadocJar.Javadoc(),
                sourcesJar = true,
            )
        )

        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)

        if (project.hasProperty("mavenCentralUsername")) {
            signAllPublications()
        }

        coordinates(project.group.toString(), project.name, project.version.toString())

        pom {
            name.set(project.name)
            description.set("LuxAPI ${project.name} module")
            url.set("https://github.com/Nova-Co/LuxAPI")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://github.com/Nova-Co/LuxAPI/blob/main/LICENSE")
                }
            }

            developers {
                developer {
                    id.set("nova-co")
                    name.set("Nova Co.")
                }
            }

            scm {
                url.set("https://github.com/Nova-Co/LuxAPI")
                connection.set("scm:git:https://github.com/Nova-Co/LuxAPI.git")
            }
        }
    }
}
