package com.novaco.luxapi.database.service

import com.novaco.luxapi.commons.LuxAPI
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.CompletableFuture

/**
 * Fluent, immutable builder for INSERT/UPDATE/DELETE statements against a [DatabaseService].
 */
class UpdateBuilder internal constructor(
    private val service: DatabaseService,
    private val sql: String,
    private val params: Array<out Any?> = emptyArray()
) {

    private val logger = LoggerFactory.getLogger(UpdateBuilder::class.java)

    fun bind(vararg params: Any?): UpdateBuilder =
        UpdateBuilder(service, sql, params)

    fun execute(): Int {
        return try {
            service.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    SqlBinder.bindAll(statement, params)
                    statement.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            logger.error("Error executing SQL update ({})", sql, e)
            -1
        }
    }

    fun executeInsert(): Long? {
        return try {
            service.getConnection().use { connection ->
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { statement ->
                    SqlBinder.bindAll(statement, params)
                    statement.executeUpdate()

                    statement.generatedKeys.use { keys ->
                        if (keys.next()) keys.getLong(1) else null
                    }
                }
            }
        } catch (e: SQLException) {
            logger.error("Error executing SQL insert ({})", sql, e)
            null
        }
    }

    fun executeAsync(): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()

        LuxAPI.getScheduler().runAsync {
            try {
                future.complete(execute())
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    fun executeInsertAsync(): CompletableFuture<Long?> {
        val future = CompletableFuture<Long?>()

        LuxAPI.getScheduler().runAsync {
            try {
                future.complete(executeInsert())
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }
}
