package com.novaco.luxapi.core.server

import net.minecraft.server.MinecraftServer

/**
 * A safe cross-platform wrapper to access the active MinecraftServer instance.
 * The platform initializer (Fabric/NeoForge) MUST set this value when the server starts.
 */
object LuxServerManager {

    private var activeServer: MinecraftServer? = null

    /**
     * Initializes the server manager. Must be called by the platform entrypoint.
     */
    fun init(server: MinecraftServer) {
        this.activeServer = server
    }

    /**
     * Clears the server instance. Must be called when the server stops.
     */
    fun clear() {
        this.activeServer = null
    }

    /**
     * Retrieves the currently active MinecraftServer.
     * @throws IllegalStateException if the server is not yet running or loaded.
     */
    fun getServer(): MinecraftServer {
        return activeServer ?: throw IllegalStateException("LuxAPI: MinecraftServer is not initialized yet!")
    }

    /**
     * Safe unboxing for nullable checks.
     */
    fun getServerOrNull(): MinecraftServer? {
        return activeServer
    }
}