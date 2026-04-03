plugins {
    kotlin("jvm")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":commons"))

    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
}

kotlin {
    jvmToolchain(21)
}