package com.novaco.luxapi.database.service.impl

import com.novaco.luxapi.database.config.DatabaseConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.sql.SQLException

class HikariDatabaseProviderTest {

    @Test
    fun `test sqlite connection pool creation and raw sql execution`(@TempDir tempDir: File) {
        // 1. Setup config for SQLite
        val config = DatabaseConfig().apply {
            type = "SQLITE"
            databaseName = "test_luxapi"
        }

        // 2. Initialize the Provider
        val provider = HikariDatabaseProvider(config, tempDir)

        // 3. Verify the file was physically created
        val expectedDbFile = File(tempDir, "test_luxapi.db")
        assertTrue(expectedDbFile.exists(), "The SQLite database file must be created in the data folder.")

        // 4. Test actual SQL Execution (Create & Insert)
        provider.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT);")
                statement.execute("INSERT INTO users (name) VALUES ('NovacoAdmin');")
            }
        }

        // 5. Test SQL Retrieval (Select)
        provider.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                val resultSet = statement.executeQuery("SELECT * FROM users WHERE id = 1;")

                assertTrue(resultSet.next(), "The result set should contain our inserted row.")
                assertEquals("NovacoAdmin", resultSet.getString("name"), "The retrieved data must match the inserted data.")
            }
        }

        // 6. Test graceful shutdown
        provider.close()

        // Ensure connection throws exception after close
        assertThrows(SQLException::class.java) {
            provider.getConnection()
        }
    }

    @Test
    fun `test database type case insensitivity`(@TempDir tempDir: File) {
        // Test that "sqlite" (lowercase) still routes to the SQLite setup
        val config = DatabaseConfig().apply {
            type = "sqlite"
            databaseName = "case_test"
        }

        val provider = HikariDatabaseProvider(config, tempDir)

        // If it successfully gets a connection without throwing a MySQL driver error, the routing worked
        assertDoesNotThrow {
            provider.getConnection().close()
        }

        provider.close()
    }
}