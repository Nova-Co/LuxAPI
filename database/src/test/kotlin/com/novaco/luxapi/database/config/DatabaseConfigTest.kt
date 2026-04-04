package com.novaco.luxapi.database.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DatabaseConfigTest {

    @Test
    fun `test default database config values`() {
        val config = DatabaseConfig()

        assertEquals("SQLITE", config.type, "Default database type should be SQLITE.")
        assertEquals("127.0.0.1", config.host, "Default host should be localhost.")
        assertEquals(3306, config.port, "Default port should be 3306.")
        assertEquals("lux_data", config.databaseName, "Default database name should match.")
        assertEquals("root", config.username)
        assertEquals("password", config.password)
    }

    @Test
    fun `test config values are mutable`() {
        val config = DatabaseConfig()
        config.type = "MYSQL"
        config.port = 3307

        assertEquals("MYSQL", config.type)
        assertEquals(3307, config.port)
    }
}