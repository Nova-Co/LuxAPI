package com.novaco.luxapi.cobblemon.npc.manager

import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.ConcurrentHashMap

/**
 * A global registry for NPC interaction logic.
 * Ensures that custom Kotlin interactions survive server restarts by decoupling
 * the logic from the ephemeral NPC Builder instance.
 */
object LuxInteractionRegistry {

    private val interactions = ConcurrentHashMap<String, (ServerPlayer, NPCEntity) -> Unit>()

    /**
     * Registers a persistent interaction handler.
     * Must be called during server startup (e.g., inside LuxCobblemon.init or your mod's init).
     *
     * @param interactId The unique ID for this interaction logic.
     * @param action The Kotlin lambda to execute.
     */
    fun register(interactId: String, action: (ServerPlayer, NPCEntity) -> Unit) {
        interactions[interactId] = action
    }

    /**
     * Retrieves a registered interaction handler.
     */
    fun get(interactId: String): ((ServerPlayer, NPCEntity) -> Unit)? {
        return interactions[interactId]
    }

    /**
     * Clears all registered interactions (useful for server reloads).
     */
    fun clear() {
        interactions.clear()
    }
}