package com.novaco.luxapi.database.migration

import com.novaco.luxapi.database.config.DatabaseConfig
import com.novaco.luxapi.database.service.DatabaseService
import com.novaco.luxapi.database.service.impl.HikariDatabaseProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.sql.SQLException

class MigrationRunnerTest {

    private lateinit var service: DatabaseService

    @BeforeEach
    fun setUp(@TempDir tempDir: File) {
        MigrationRunner.reset()

        val config = DatabaseConfig().apply {
            type = "SQLITE"
            databaseName = "migration_runner_test"
        }
        service = HikariDatabaseProvider(config, tempDir)
    }

    @AfterEach
    fun tearDown() {
        MigrationRunner.reset()
        service.close()
    }

    @Test
    fun `test applies pending migrations in version order`() {
        val applied = mutableListOf<Int>()

        MigrationRunner.register(
            "economy",
            Migration(2, "second") { applied.add(2) },
            Migration(1, "first") { applied.add(1) }
        )

        MigrationRunner.applyPending(service)

        assertEquals(listOf(1, 2), applied)
    }

    @Test
    fun `test does not reapply already applied migrations`() {
        var runCount = 0

        MigrationRunner.register("economy", Migration(1, "create table") { runCount++ })

        MigrationRunner.applyPending(service)
        MigrationRunner.applyPending(service)

        assertEquals(1, runCount)
    }

    @Test
    fun `test modules with overlapping version numbers do not collide`() {
        val economyApplied = mutableListOf<Int>()
        val attributeApplied = mutableListOf<Int>()

        MigrationRunner.register("economy", Migration(1, "economy v1") { economyApplied.add(1) })
        MigrationRunner.register("attribute", Migration(1, "attribute v1") { attributeApplied.add(1) })

        MigrationRunner.applyPending(service)

        assertEquals(listOf(1), economyApplied)
        assertEquals(listOf(1), attributeApplied)
    }

    @Test
    fun `test failed migration rolls back and aborts`() {
        MigrationRunner.register(
            "economy",
            Migration(1, "broken") { conn ->
                conn.createStatement().use { it.execute("CREATE TABLE t (id INTEGER PRIMARY KEY)") }
                throw SQLException("simulated failure")
            }
        )

        assertThrows(SQLException::class.java) {
            MigrationRunner.applyPending(service)
        }

        service.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                val resultSet = statement.executeQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='t'"
                )
                resultSet.next()
                assertEquals(0, resultSet.getInt(1), "Table creation must be rolled back on migration failure.")
            }
        }
    }
}
