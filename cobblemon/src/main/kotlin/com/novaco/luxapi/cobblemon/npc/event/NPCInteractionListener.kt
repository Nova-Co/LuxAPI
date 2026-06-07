package com.novaco.luxapi.cobblemon.npc.event

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.cobblemon.npc.manager.InteractionRegistry
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

object NPCInteractionListener {

    /**
     * Call this from your platform-specific EntityInteract event.
     *
     * @param player The player initiating the interaction.
     * @param target The entity being clicked.
     * @return True if LuxAPI handled the interaction, False otherwise.
     */
    fun onInteract(player: ServerPlayer, target: Entity): Boolean {
        if (target !is NPCEntity) return false

        // Look for the special tag we added in LuxNPCBuilder
        val interactTag = target.tags.firstOrNull { it.startsWith("lux_interact:") } ?: return false

        // Extract the ID (e.g., "lux_interact:gym_brock" -> "gym_brock")
        val interactId = interactTag.substringAfter("lux_interact:")

        // Fetch and execute the logic
        val handler = InteractionRegistry.get(interactId)
        if (handler != null) {
            handler.invoke(player, target)
            return true // We handled it, cancel native processing
        }

        return false
    }
}