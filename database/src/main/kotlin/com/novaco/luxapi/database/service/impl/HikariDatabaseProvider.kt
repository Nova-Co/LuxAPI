package com.novaco.luxapi.database.service.impl

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.database.config.DatabaseConfig
import com.novaco.luxapi.database.service.DatabaseService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.sql.Connection
import java.util.concurrent.CompletableFuture

/**
 * The HikariCP implementation of the DatabaseService.
 * Automatically routes and pools connections for both SQLite and MySQL/MariaDB environments.
 */
class HikariDatabaseProvider(
    private val config: DatabaseConfig,
    private val dataFolder: File
) : DatabaseService {

    private val dataSource: HikariDataSource

    init {
        val hikariConfig = HikariConfig()

        if (config.type.equals("SQLITE", ignoreCase = true)) {
            setupSQLite(hikariConfig)
        } else {
            setupMySQL(hikariConfig)
        }

        hikariConfig.connectionTimeout = 10000
        hikariConfig.leakDetectionThreshold = 5000
        hikariConfig.poolName = "LuxAPI-DbPool"

        this.dataSource = HikariDataSource(hikariConfig)
    }

    /**
     * Configures the Hikari pool specifically for local SQLite files.
     */
    private fun setupSQLite(hikariConfig: HikariConfig) {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        val dbFile = File(dataFolder, "${config.databaseName}.db")

        hikariConfig.jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
        hikariConfig.driverClassName = "org.sqlite.JDBC"

        hikariConfig.maximumPoolSize = 1
        hikariConfig.connectionTestQuery = "SELECT 1"
    }

    /**
     * Configures the Hikari pool for remote MySQL or MariaDB servers.
     */
    private fun setupMySQL(hikariConfig: HikariConfig) {
        val protocol = if (config.type.equals("MARIADB", ignoreCase = true)) "mariadb" else "mysql"

        hikariConfig.jdbcUrl = "jdbc:$protocol://${config.host}:${config.port}/${config.databaseName}?useSSL=false&autoReconnect=true"
        hikariConfig.username = config.username
        hikariConfig.password = config.password

        hikariConfig.driverClassName = if (protocol == "mariadb") "org.mariadb.jdbc.Driver" else "com.mysql.cj.jdbc.Driver"

        hikariConfig.maximumPoolSize = 10
        hikariConfig.minimumIdle = 2

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    }

    /**
     * Retrieves an active connection from the Hikari pool.
     *
     * @return A valid SQL Connection object.
     */
    override fun getConnection(): Connection {
        return dataSource.connection
    }

    /**
     * Executes a database query asynchronously using LuxAPI's cross-platform async scheduler.
     *
     * @param query The SQL operation to execute.
     * @return A CompletableFuture representing the async execution.
     */
    override fun executeAsync(query: (Connection) -> Unit): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()

        LuxAPI.getScheduler().runAsync {
            try {
                getConnection().use { connection ->
                    query(connection)
                }
                future.complete(null)
            } catch (e: Exception) {
                println("[LuxAPI] An error occurred during an asynchronous database operation:")
                e.printStackTrace()
                future.completeExceptionally(e)
            }
        }

        return future
    }

    /**
     * Safely shuts down the connection pool, closing all active connections.
     */
    override fun close() {
        if (!dataSource.isClosed) {
            dataSource.close()
        }
    }
}