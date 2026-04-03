package com.novaco.luxapi.cobblemon

import com.novaco.luxapi.cobblemon.listener.CobblemonEventHandler

/**
 * The primary entry point for the LuxAPI Cobblemon module.
 * Handles the initialization of Cobblemon-specific features and listeners.
 *
 * @author NovaCo
 */
object LuxCobblemon {

    /**
     * Initializes all Cobblemon integrations.
     * Must be called during the main mod initialization phase (e.g., in Fabric/NeoForge entry points).
     */
    fun init() {
        CobblemonEventHandler.register()
    }
}