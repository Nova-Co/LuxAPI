package com.novaco.luxapi.database

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.database.attribute.AttributeManager
import com.novaco.luxapi.database.migration.MigrationRunner
import com.novaco.luxapi.database.service.DatabaseService

/**
 * The main entry point for the lux-database module.
 */
object LuxDatabase {

    /**
     * Initializes the database module by hooking necessary listeners into the EventBus
     * and running any pending schema migrations registered via
     * [DatabaseService.registerMigrations].
     * This should be called by your main plugin/mod class during server startup.
     */
    fun init() {
        EventBus.register(AttributeManager)

        LuxAPI.getService<DatabaseService>()?.let { service ->
            MigrationRunner.applyPending(service)
        }

        println("[LuxAPI] Database module initialized successfully.")
    }
}