package com.novaco.luxapi.cobblemon.manager

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.platform.events.PlatformEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType

/**
 * Manages global interactions with Cobblemon NPCs across all platforms.
 * Handles automatic behaviors defined by LuxAPI builders.
 */
object NPCInteractionManager {

    /**
     * Registers platform-independent event listeners for NPC interactions.
     */
    fun register() {
        PlatformEvents.RIGHT_CLICK_ENTITY.subscribe { event ->
            if (event.hand == InteractionHand.MAIN_HAND) {
                val entity = event.entity

                if (entity is NPCEntity) {
                    if (entity.tags.contains("lux_look_at_interactor")) {
                        val entityTracker = EntityTracker(event.player, true)
                        entity.brain.setMemory(MemoryModuleType.LOOK_TARGET, entityTracker)
                    }
                }
            }
        }
    }
}