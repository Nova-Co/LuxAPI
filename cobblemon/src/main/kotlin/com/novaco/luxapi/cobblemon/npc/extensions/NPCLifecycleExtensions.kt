package com.novaco.luxapi.cobblemon.npc.extensions

import com.novaco.luxapi.cobblemon.npc.manager.NPCManager
import net.minecraft.server.MinecraftServer

/**
 * Extension utility functions for managing LuxAPI NPCs gracefully.
 */

/**
 * Despawns an NPC created by LuxAPI using its developer-assigned ID.
 * * Example:
 * ```
 * server.despawnNPC("tutorial_guide_01")
 * ```
 * * @param customId The ID provided during the LuxNPCBuilder configuration.
 * @return True if the NPC was removed, false if it could not be found.
 */
fun MinecraftServer.despawnNPC(customId: String): Boolean {
    return NPCManager.removeNPC(customId, this)
}

/**
 * Checks if a specific LuxAPI NPC is currently registered and loaded.
 */
fun MinecraftServer.isNPCLoaded(customId: String): Boolean {
    return NPCManager.getNPC(customId, this) != null
}