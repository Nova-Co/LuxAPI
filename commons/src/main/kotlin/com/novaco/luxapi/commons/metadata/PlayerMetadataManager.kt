package com.novaco.luxapi.commons.metadata

import com.novaco.luxapi.commons.event.Subscribe
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized manager for player-specific metadata.
 * Automatically cleans up data to prevent memory leaks when a player disconnects.
 */
object PlayerMetadataManager {

    private val playerContainers = ConcurrentHashMap<UUID, MetadataContainer>()

    /**
     * Gets or creates a metadata container for the specified player UUID.
     */
    fun getContainer(uuid: UUID): MetadataContainer {
        return playerContainers.computeIfAbsent(uuid) { MetadataContainer() }
    }

    /**
     * Internal event listener to automatically destroy metadata
     * when a player leaves the server.
     */
    @Subscribe
    internal fun onPlayerQuit(event: PlayerQuitEvent) {
        playerContainers.remove(event.player.uniqueId)
    }
}