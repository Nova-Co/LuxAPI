package com.novaco.luxapi.database.service

import com.novaco.luxapi.commons.LuxAPI
import org.slf4j.LoggerFactory
import java.io.IOException
import java.sql.ResultSet
import java.sql.SQLException
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException

/**
 * Fluent, immutable builder for SELECT queries against a [DatabaseService].
 * Each mutator (`bind`, `map`) returns a new instance; the receiver is left untouched.
 */
class QueryBuilder<T> internal constructor(
    private val service: DatabaseService,
    private val sql: String,
    private val params: Array<out Any?> = emptyArray(),
    private val mapper: ((ResultSet) -> T)? = null
) {

    private val logger = LoggerFactory.getLogger(QueryBuilder::class.java)

    fun bind(vararg params: Any?): QueryBuilder<T> =
        QueryBuilder(service, sql, params, mapper)

    fun <R> map(mapper: (ResultSet) -> R): QueryBuilder<R> =
        QueryBuilder(service, sql, params, mapper)

    fun execute(): List<T> {
        val currentMapper = mapper
            ?: throw IllegalStateException("QueryBuilder requires map() to be called before execute()")

        return try {
            service.getConnection().use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    SqlBinder.bindAll(statement, params)

                    statement.executeQuery().use { resultSet ->
                        val results = mutableListOf<T>()

                        while (resultSet.next()) {
                            results.add(currentMapper(resultSet))
                        }

                        results
                    }
                }
            }
        } catch (e: SQLException) {
            logger.error("Error executing SQL query ({})", sql, e)
            emptyList()
        }
    }

    fun executeOne(): T? = execute().firstOrNull()

    fun executeAsync(): CompletableFuture<List<T>> {
        val future = CompletableFuture<List<T>>()

        LuxAPI.getScheduler().runAsync {
            try {
                future.complete(execute())
            } catch (e: SQLException) {
                future.completeExceptionally(e)
            } catch (e: IOException) {
                future.completeExceptionally(e)
            } catch (e: TimeoutException) {
                future.completeExceptionally(e)
            } catch (e: CancellationException) {
                future.completeExceptionally(e)
            } catch (e: IllegalStateException) {
                future.completeExceptionally(e)
            } catch (e: IllegalArgumentException) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    fun executeOneAsync(): CompletableFuture<T?> =
        executeAsync().thenApply { it.firstOrNull() }
}
