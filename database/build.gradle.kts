plugins {
    kotlin("jvm")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":commons"))

    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")

    // --- Unit Testing Framework ---
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito:mockito-core:5.11.0")
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