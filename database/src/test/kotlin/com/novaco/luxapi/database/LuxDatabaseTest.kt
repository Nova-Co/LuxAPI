package com.novaco.luxapi.database

import com.novaco.luxapi.commons.service.ServiceManager
import com.novaco.luxapi.database.config.DatabaseConfig
import com.novaco.luxapi.database.migration.Migration
import com.novaco.luxapi.database.migration.MigrationRunner
import com.novaco.luxapi.database.service.DatabaseService
import com.novaco.luxapi.database.service.impl.HikariDatabaseProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class LuxDatabaseTest {

    @AfterEach
    fun tearDown() {
        MigrationRunner.reset()
        ServiceManager.clear()
    }

    @Test
    fun `test database module initializes without exception`() {
        // Should securely register the manager to the EventBus without throwing errors
        assertDoesNotThrow {
            LuxDatabase.init()
        }
    }

    @Test
    fun `test init runs pending migrations when a DatabaseService is registered`(@TempDir tempDir: File) {
        val config = DatabaseConfig().apply {
            type = "SQLITE"
            databaseName = "lux_database_init_test"
        }
        val service = HikariDatabaseProvider(config, tempDir)
        ServiceManager.register(DatabaseService::class.java, service)

        var migrationRan = false
        MigrationRunner.register("test-module", Migration(1, "marker") { migrationRan = true })

        LuxDatabase.init()

        assertTrue(migrationRan, "LuxDatabase.init() must run pending migrations when a DatabaseService is registered.")

        service.close()
    }
}
