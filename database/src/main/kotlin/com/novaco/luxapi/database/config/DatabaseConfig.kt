package com.novaco.luxapi.database.config

import com.novaco.luxapi.commons.config.LuxConfig
import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config

/**
 * Configuration file for database connection credentials.
 * Automatically generated and managed by the LuxAPI ConfigService.
 */
@Config("database.yml")
@Comment("Configure your SQL database connection here. Supported types: SQLITE, MYSQL, MARIADB")
class DatabaseConfig : LuxConfig() {

    @Comment("The type of database to use (SQLITE or MYSQL)")
    var type: String = "SQLITE"

    @Comment("The hostname or IP address of the database server (Not used for SQLite)")
    var host: String = "127.0.0.1"

    @Comment("The port of the database server (Usually 3306 for MySQL)")
    var port: Int = 3306

    @Comment("The name of the database schema (For SQLite, this will be the file name: database.db)")
    var databaseName: String = "lux_data"

    @Comment("The username for the database connection")
    var username: String = "root"

    @Comment("The password for the database connection")
    var password: String = "password"
}