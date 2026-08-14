package com.novaco.luxapi.database.service

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.scheduler.LuxScheduler
import com.novaco.luxapi.commons.scheduler.LuxTask
import com.novaco.luxapi.database.config.DatabaseConfig
import com.novaco.luxapi.database.service.impl.HikariDatabaseProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private class UpdateFakeLuxTask(override val id: Int = 1, override val isAsync: Boolean = true) : LuxTask {
    override var isCancelled: Boolean = false
    override fun cancel() { isCancelled = true }
}

private class UpdateSyncScheduler : LuxScheduler {
    override fun run(runnable: Runnable): LuxTask { runnable.run(); return UpdateFakeLuxTask() }
    override fun runAsync(runnable: Runnable): LuxTask { runnable.run(); return UpdateFakeLuxTask() }
    override fun runLater(delay: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runLaterAsync(delay: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runRepeating(delay: Long, period: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runRepeatingAsync(delay: Long, period: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun cancelAll() {}
}

class UpdateBuilderTest {

    private lateinit var service: DatabaseService

    @BeforeEach
    fun setUp(@TempDir tempDir: File) {
        LuxAPI.schedulerProvider = { UpdateSyncScheduler() }

        val config = DatabaseConfig().apply {
            type = "SQLITE"
            databaseName = "update_builder_test"
        }
        service = HikariDatabaseProvider(config, tempDir)

        service.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    "CREATE TABLE wallets (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL, balance REAL NOT NULL)"
                )
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
    fun `test execute returns rows affected`() {
        val inserted = service.update("INSERT INTO wallets (uuid, balance) VALUES (?, ?)")
            .bind("a-uuid", 50.0)
            .execute()

        assertEquals(1, inserted)

        val updated = service.update("UPDATE wallets SET balance = ? WHERE uuid = ?")
            .bind(75.0, "a-uuid")
            .execute()

        assertEquals(1, updated)
    }

    @Test
    fun `test executeInsert returns generated key`() {
        val key = service.update("INSERT INTO wallets (uuid, balance) VALUES (?, ?)")
            .bind("b-uuid", 10.0)
            .executeInsert()

        assertNotNull(key)
        assertEquals(1L, key)
    }

    @Test
    fun `test executeAsync completes with rows affected`() {
        val future = service.update("INSERT INTO wallets (uuid, balance) VALUES (?, ?)")
            .bind("c-uuid", 5.0)
            .executeAsync()

        assertEquals(1, future.get())
    }

    @Test
    fun `test malformed sql returns negative one instead of throwing`() {
        val result = service.update("UPDATE nonexistent_table SET balance = ? WHERE uuid = ?")
            .bind(1.0, "a-uuid")
            .execute()

        assertEquals(-1, result)
    }

    @Test
    fun `test executeInsert on malformed sql returns null`() {
        val key = service.update("INSERT INTO nonexistent_table (uuid) VALUES (?)")
            .bind("z-uuid")
            .executeInsert()

        assertNull(key)
    }
}
