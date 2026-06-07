package com.novaco.luxapi.cobblemon.npc.battle

import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * A global registry for managing post-battle interaction logic.
 */
object BattleRegistry {

    private val battleCallbacks = ConcurrentHashMap<UUID, (ServerPlayer, NPCEntity, BattleResult) -> Unit>()

    /**
     * Registers a persistent post-battle callback for a specific NPC.
     *
     * @param npcUuid The unique identifier of the NPCEntity.
     * @param action The callback to execute when the battle ends.
     */
    fun register(npcUuid: UUID, action: (ServerPlayer, NPCEntity, BattleResult) -> Unit) {
        battleCallbacks[npcUuid] = action
    }

    /**
     * Retrieves the registered post-battle callback for the given NPC.
     *
     * @param npcUuid The unique identifier of the NPCEntity.
     * @return The callback function, or null if none is registered.
     */
    fun get(npcUuid: UUID): ((ServerPlayer, NPCEntity, BattleResult) -> Unit)? {
        return battleCallbacks[npcUuid]
    }

    /**
     * Removes the registered post-battle callback for the given NPC.
     *
     * @param npcUuid The unique identifier of the NPCEntity.
     */
    fun unregister(npcUuid: UUID) {
        battleCallbacks.remove(npcUuid)
    }

    /**
     * Clears all registered battle callbacks.
     */
    fun clear() {
        battleCallbacks.clear()
    }
}