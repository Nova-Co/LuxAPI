package com.novaco.luxapi.database.migration

import com.novaco.luxapi.database.service.DatabaseService
import org.slf4j.LoggerFactory
import java.sql.SQLException

/**
 * Runs pending [Migration]s registered per module against a shared
 * `schema_migrations(module, version)` ledger table. Modules' version
 * sequences are independent — namespacing by `module` means overlapping
 * version numbers across modules never collide.
 */
object MigrationRunner {

    private val logger = LoggerFactory.getLogger(MigrationRunner::class.java)
    private val registry = mutableMapOf<String, MutableList<Migration>>()

    fun register(module: String, vararg migrations: Migration) {
        registry.getOrPut(module) { mutableListOf() }.addAll(migrations)
    }

    /**
     * Clears all registered migrations. Test-only — prevents state leaking
     * across test runs through this singleton's registry.
     */
    internal fun reset() {
        registry.clear()
    }

    fun applyPending(service: DatabaseService) {
        ensureMigrationsTable(service)

        for ((module, migrations) in registry) {
            val appliedVersions = getAppliedVersions(service, module)

            for (migration in migrations.sortedBy { it.version }) {
                if (migration.version in appliedVersions) {
                    continue
                }

                applyMigration(service, module, migration)
            }
        }
    }

    private fun ensureMigrationsTable(service: DatabaseService) {
        service.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS schema_migrations (
                        module TEXT NOT NULL,
                        version INTEGER NOT NULL,
                        applied_at INTEGER NOT NULL,
                        PRIMARY KEY (module, version)
                    )
                    """.trimIndent()
                )
            }
        }
    }

    private fun getAppliedVersions(service: DatabaseService, module: String): Set<Int> {
        service.getConnection().use { connection ->
            connection.prepareStatement(
                "SELECT version FROM schema_migrations WHERE module = ?"
            ).use { statement ->
                statement.setString(1, module)

                statement.executeQuery().use { resultSet ->
                    val versions = mutableSetOf<Int>()

                    while (resultSet.next()) {
                        versions.add(resultSet.getInt("version"))
                    }

                    return versions
                }
            }
        }
    }

    private fun applyMigration(service: DatabaseService, module: String, migration: Migration) {
        service.getConnection().use { connection ->
            val originalAutoCommit = connection.autoCommit
            connection.autoCommit = false

            try {
                migration.apply(connection)

                connection.prepareStatement(
                    "INSERT INTO schema_migrations (module, version, applied_at) VALUES (?, ?, ?)"
                ).use { statement ->
                    statement.setString(1, module)
                    statement.setInt(2, migration.version)
                    statement.setLong(3, System.currentTimeMillis())
                    statement.executeUpdate()
                }

                connection.commit()
                logger.info("Applied migration {}:{} - {}", module, migration.version, migration.description)
            } catch (e: SQLException) {
                connection.rollback()
                logger.error("Migration {}:{} failed - {}", module, migration.version, migration.description, e)
                throw e
            } finally {
                connection.autoCommit = originalAutoCommit
            }
        }
    }
}
