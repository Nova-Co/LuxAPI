package com.novaco.luxapi.cobblemon

import com.novaco.luxapi.cobblemon.boss.BossDefeatListener
import com.novaco.luxapi.cobblemon.listener.CobblemonEventHandler
import com.novaco.luxapi.cobblemon.listener.UncatchableManager
import com.novaco.luxapi.cobblemon.manager.NPCInteractionManager

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