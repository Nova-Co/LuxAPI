package com.novaco.luxapi.cobblemon.npc.manager

import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.server.MinecraftServer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * A central registry for managing custom NPCs spawned via LuxAPI.
 * Allows developers to retrieve, track, and safely despawn NPCs using custom String IDs.
 */
object NPCManager {

    // Maps a developer-defined String ID to the Minecraft Entity UUID
    private val npcRegistry = ConcurrentHashMap<String, UUID>()

    /**
     * Registers a newly spawned NPC into the manager.
     */
    fun registerNPC(customId: String, entityUuid: UUID) {
        npcRegistry[customId] = entityUuid
    }

    /**
     * Unregisters an NPC from the manager.
     */
    fun unregisterNPC(customId: String) {
        npcRegistry.remove(customId)
    }

    /**
     * Retrieves an active NPCEntity across all server dimensions using its custom ID.
     * * @param customId The ID assigned during building.
     * @param server The active Minecraft Server instance.
     * @return The [NPCEntity] if loaded and alive, null otherwise.
     */
    fun getNPC(customId: String, server: MinecraftServer): NPCEntity? {
        val uuid = npcRegistry[customId] ?: return null

        return server.allLevels
            .mapNotNull { it.getEntity(uuid) }
            .firstOrNull() as? NPCEntity
    }

    /**
     * Safely despawns and removes an NPC from the world and registry.
     * * @param customId The custom ID of the NPC.
     * @param server The active Minecraft Server instance.
     * @return True if the NPC was successfully found and removed.
     */
    fun removeNPC(customId: String, server: MinecraftServer): Boolean {
        val entity = getNPC(customId, server) ?: return false

        entity.discard() // Safely removes the entity from the Minecraft world
        unregisterNPC(customId)

        // Also unregister from head-tracking if applicable
        com.novaco.luxapi.cobblemon.npc.tracker.NPCTracker.unregister(entity.uuid)

        return true
    }

    /**
     * Gets all currently registered NPC IDs.
     */
    fun getRegisteredIds(): Set<String> {
        return npcRegistry.keys.toSet()
    }
}