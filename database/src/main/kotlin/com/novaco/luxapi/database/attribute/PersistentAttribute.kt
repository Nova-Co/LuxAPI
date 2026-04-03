package com.novaco.luxapi.database.attribute

import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.scheduler.LuxTask
import com.novaco.luxapi.database.service.DatabaseService
import java.util.UUID

/**
 * The base template for all persistent player data in the LuxAPI ecosystem.
 * Handles automatic asynchronous loading and saving to prevent main-thread lag.
 * * @param uuid The unique identifier of the player this data belongs to.
 */
abstract class PersistentAttribute(val uuid: UUID) {

    /**
     * Internal state to track if the data has been successfully loaded from the database.
     */
    var isLoaded: Boolean = false
        protected set

    private var autoSaveTask: LuxTask? = null

    /**
     * Defines the SQL logic to pull data from the database and populate this object.
     * * @param service The active database service to use for queries.
     */
    abstract fun loadData(service: DatabaseService)

    /**
     * Defines the SQL logic to push the current state of this object into the database.
     *
     * @param service The active database service to use for queries.
     */
    abstract fun saveData(service: DatabaseService)

    /**
     * Triggers an asynchronous load operation.
     */
    fun loadAsync() {
        val service = LuxAPI.getService<DatabaseService>() ?: return
        service.executeAsync {
            loadData(service)
            isLoaded = true
        }
    }

    /**
     * Triggers an asynchronous save operation.
     */
    fun saveAsync() {
        val service = LuxAPI.getService<DatabaseService>() ?: return
        if (isLoaded) {
            service.executeAsync { saveData(service) }
        }
    }

    /**
     * Starts an automatic save timer for this specific attribute.
     * * @param intervalTicks The time between saves in Minecraft ticks (e.g., 6000 for 5 minutes).
     */
    fun enableAutoSave(intervalTicks: Long) {
        autoSaveTask?.cancel()
        autoSaveTask = LuxAPI.getScheduler().runRepeatingAsync(intervalTicks, intervalTicks) {
            saveAsync()
        }
    }

    /**
     * Stops the automatic save timer and forces one final save.
     */
    fun disableAutoSave() {
        autoSaveTask?.cancel()
        saveAsync()
    }
}