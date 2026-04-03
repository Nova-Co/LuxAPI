package com.novaco.luxapi.database

import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.database.attribute.AttributeManager

/**
 * The main entry point for the lux-database module.
 */
object LuxDatabase {

    /**
     * Initializes the database module by hooking necessary listeners into the EventBus.
     * This should be called by your main plugin/mod class during server startup.
     */
    fun init() {
        EventBus.register(AttributeManager)

        println("[LuxAPI] Database module initialized successfully.")
    }
}