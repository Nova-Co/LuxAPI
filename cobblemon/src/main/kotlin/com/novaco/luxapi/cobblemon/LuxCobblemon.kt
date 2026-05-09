package com.novaco.luxapi.cobblemon

import com.novaco.luxapi.cobblemon.boss.BossDefeatListener
import com.novaco.luxapi.cobblemon.listener.CobblemonEventHandler
import com.novaco.luxapi.cobblemon.listener.UncatchableManager
import com.novaco.luxapi.cobblemon.manager.NPCInteractionManager
import com.novaco.luxapi.cobblemon.npc.tracker.LuxNPCTracker
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.core.server.LuxServerManager

/**
 * The primary entry point for the LuxAPI Cobblemon module.
 * Handles the initialization of Cobblemon-specific features, listeners, and the Boss Framework.
 */
object LuxCobblemon {

    private var isInitialized = false

    /**
     * Initializes all Cobblemon integrations.
     * Must be called during the main mod initialization phase (e.g., in Fabric/NeoForge entry points).
     */
    fun init() {
        if (isInitialized) return

        LuxAPI.getScheduler().runRepeating(0L, 1L) {

            // Fetch the server instance safely from Core manager
            val server = LuxServerManager.getServerOrNull()

            if (server != null) {
                // Execute the math logic for all active stationary NPCs
                LuxNPCTracker.tick(server)
            }
        }

        // Core Registrations
        CobblemonEventHandler.register()
        NPCInteractionManager.register()

        // Boss Framework Registrations
        BossDefeatListener.register()
        UncatchableManager.register()

        isInitialized = true
        println("[LuxAPI] Cobblemon module and Boss Framework initialized successfully!")
    }
}