package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File

/**
 * A concrete configuration class used strictly for testing File I/O operations.
 * FIXED: Added @ConfigSerializable so Sponge Configurate can safely map the YAML data.
 */
@ConfigSerializable
@Config("database.yml")
@Comment("Database connection settings")
class TestDatabaseConfig : LuxConfig() {
    var host: String = "localhost"
    var port: Int = 3306
}

class ConfigServiceTest {

    @Test
    fun `test configuration generation, modification, and reloading`(@TempDir tempDir: File) {
        val config = ConfigService.load(TestDatabaseConfig::class.java, tempDir)

        val expectedFile = File(tempDir, "database.yml")
        assertTrue(expectedFile.exists(), "ConfigService should physically create the database.yml file.")
        assertEquals("localhost", config.host, "Initial value should match the class default.")
        assertEquals(3306, config.port, "Initial value should match the class default.")

        config.host = "192.168.1.100"
        config.port = 27017
        config.save()

        val reloadedConfig = TestDatabaseConfig()
        reloadedConfig.init(expectedFile)
        reloadedConfig.reload()

        assertEquals("192.168.1.100", reloadedConfig.host, "Reloaded config should retain saved modifications.")
        assertEquals(27017, reloadedConfig.port, "Reloaded config should retain saved modifications.")
    }

    @Test
    fun `test loading unannotated class throws exception`(@TempDir tempDir: File) {
        class InvalidConfig : LuxConfig()

        assertThrows(IllegalArgumentException::class.java) {
            ConfigService.load(InvalidConfig::class.java, tempDir)
        }
    }
}