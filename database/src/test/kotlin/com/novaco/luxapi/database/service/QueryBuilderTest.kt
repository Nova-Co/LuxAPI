package com.novaco.luxapi.database.service

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.scheduler.LuxScheduler
import com.novaco.luxapi.commons.scheduler.LuxTask
import com.novaco.luxapi.database.config.DatabaseConfig
import com.novaco.luxapi.database.service.impl.HikariDatabaseProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private class QueryFakeLuxTask(override val id: Int = 1, override val isAsync: Boolean = true) : LuxTask {
    override var isCancelled: Boolean = false
    override fun cancel() { isCancelled = true }
}

private class QuerySyncScheduler : LuxScheduler {
    override fun run(runnable: Runnable): LuxTask { runnable.run(); return QueryFakeLuxTask() }
    override fun runAsync(runnable: Runnable): LuxTask { runnable.run(); return QueryFakeLuxTask() }
    override fun runLater(delay: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runLaterAsync(delay: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runRepeating(delay: Long, period: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runRepeatingAsync(delay: Long, period: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun cancelAll() {}
}

class QueryBuilderTest {

    private lateinit var service: DatabaseService

    @BeforeEach
    fun setUp(@TempDir tempDir: File) {
        LuxAPI.schedulerProvider = { QuerySyncScheduler() }

        val config = DatabaseConfig().apply {
            type = "SQLITE"
            databaseName = "query_builder_test"
        }
        service = HikariDatabaseProvider(config, tempDir)

        service.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE wallets (uuid TEXT PRIMARY KEY, balance REAL NOT NULL)")
                statement.execute("INSERT INTO wallets (uuid, balance) VALUES ('a-uuid', 42.5)")
                statement.execute("INSERT INTO wallets (uuid, balance) VALUES ('b-uuid', 10.0)")
            }
        }
    }

    @AfterEach
    fun tearDown() {
        service.close()
        LuxAPI.schedulerProvider = {
            throw IllegalStateException("LuxAPI Scheduler Provider has not been initialized!")
        }
    }

    @Test
    fun `test bind and map return matching row`() {
        val results = service.query("SELECT balance FROM wallets WHERE uuid = ?")
            .bind("a-uuid")
            .map { rs -> rs.getDouble("balance") }
            .execute()

        assertEquals(listOf(42.5), results)
    }

    @Test
    fun `test executeOne returns first row or null when missing`() {
        val found = service.query("SELECT balance FROM wallets WHERE uuid = ?")
            .bind("a-uuid")
            .map { rs -> rs.getDouble("balance") }
            .executeOne()

        val missing = service.query("SELECT balance FROM wallets WHERE uuid = ?")
            .bind("missing-uuid")
            .map { rs -> rs.getDouble("balance") }
            .executeOne()

        assertEquals(42.5, found)
        assertNull(missing)
    }

    @Test
    fun `test executeAsync completes with mapped results`() {
        val future = service.query("SELECT balance FROM wallets ORDER BY uuid")
            .map { rs -> rs.getDouble("balance") }
            .executeAsync()

        assertEquals(listOf(42.5, 10.0), future.get())
    }

    @Test
    fun `test execute without map throws IllegalStateException`() {
        assertThrows(IllegalStateException::class.java) {
            service.query("SELECT balance FROM wallets").execute()
        }
    }

    @Test
    fun `test malformed sql returns empty list instead of throwing`() {
        val results = service.query("SELECT * FROM nonexistent_table")
            .map { rs -> rs.getString(1) }
            .execute()

        assertTrue(results.isEmpty())
    }
}
