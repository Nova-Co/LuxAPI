package com.novaco.luxapi.cobblemon

import com.novaco.luxapi.cobblemon.boss.BossDefeatListener
import com.novaco.luxapi.cobblemon.boss.BossHpSynchronizer
import com.novaco.luxapi.cobblemon.listener.CobblemonEventHandler
import com.novaco.luxapi.cobblemon.listener.UncatchableManager
import com.novaco.luxapi.cobblemon.manager.NPCInteractionManager
import com.novaco.luxapi.cobblemon.npc.tracker.NPCTracker
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.core.server.LuxServerManager
import org.slf4j.LoggerFactory

/**
 * The primary entry point for the LuxAPI Cobblemon module.
 * Handles the initialization of Cobblemon-specific features, listeners, and the Boss Framework.
 */
object LuxCobblemon {

    private val logger = LoggerFactory.getLogger(LuxCobblemon::class.java)
    private var isInitialized = false

    /**
     * Initializes all Cobblemon integrations.
     * Must be called during the main mod initialization phase (e.g., in Fabric/NeoForge entry points).
     */
    fun init() {
        if (isInitialized) return

        LuxAPI.getScheduler().runRepeating(0L, 1L) {
            val server = LuxServerManager.getServerOrNull()
            if (server != null) {
                NPCTracker.tick(server)
            }
            BossHpSynchronizer.tick()
        }

        // Initialize Unified Event Bridge first
        CobblemonEventHandler.register()

        // Core Registrations
        NPCInteractionManager.register()

        // Boss Framework Registrations
        BossDefeatListener.register()
        UncatchableManager.register()

        // Dynamic check for optional ecosystems (e.g., Database)
        checkOptionalModules()

        isInitialized = true
        logger.info("Cobblemon module and Unified Event Bridge initialized successfully!")
    }

    /**
     * Safely checks for the existence of external or optional modules at runtime
     * to prevent ClassNotFoundException if they are absent.
     */
    private fun checkOptionalModules() {
        try {
            Class.forName("com.novaco.luxapi.database.LuxDatabase")
            logger.info("Optional database module detected and verified successfully.")
        } catch (e: ClassNotFoundException) {
            logger.info("Optional database module not found. Skipping dynamic database features safely.")
        }
    }
}