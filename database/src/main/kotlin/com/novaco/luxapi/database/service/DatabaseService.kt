package com.novaco.luxapi.database.service

import java.sql.Connection
import java.util.concurrent.CompletableFuture

/**
 * A central service interface for managing database connections and operations.
 * Modules can retrieve this service via LuxAPI.getService(DatabaseService::class.java).
 */
interface DatabaseService {

    /**
     * Retrieves an active connection from the database pool.
     * Ensure this is used within a try-with-resources block (or Kotlin's .use { }) to prevent memory leaks.
     *
     * @return A valid SQL Connection object.
     */
    fun getConnection(): Connection

    /**
     * Executes a database query asynchronously to prevent server thread blocking.
     * * @param query The SQL query or operation to execute.
     * @return A CompletableFuture representing the async execution.
     */
    fun executeAsync(query: (Connection) -> Unit): CompletableFuture<Void>

    /**
     * Shuts down the database connection pool safely.
     */
    fun close()
}