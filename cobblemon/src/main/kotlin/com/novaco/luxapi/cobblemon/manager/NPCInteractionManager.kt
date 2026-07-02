package com.novaco.luxapi.cobblemon.manager

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.novaco.luxapi.cobblemon.npc.event.NPCInteractionListener
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand

/**
 * Manages global, ambient interactions for Cobblemon NPCs across all platforms.
 */
object NPCInteractionManager {

    /**
     * Registers the platform event listeners to supervise and handle custom interactions safely.
     */
    fun register() {
        PlatformEvents.SERVER_TICK_POST.subscribe { event ->
            val server = event.server

            for (level in server.allLevels) {
                for (player in level.players()) {
                    if (!player.isAlive || player.isSpectator) continue

                    val searchBox = player.boundingBox.inflate(7.0, 3.0, 7.0)
                    val nearbyNpcs = level.getEntitiesOfClass(NPCEntity::class.java, searchBox) { npc ->
                        npc.isAlive && npc.tags.contains("lux_look_at_interactor") && !npc.isInBattle()
                    }

                    for (npc in nearbyNpcs) {
                        npc.lookControl.setLookAt(player, 30.0f, 30.0f)
                        npc.yBodyRot = npc.yHeadRot
                    }
                }
            }
        }

        PlatformEvents.RIGHT_CLICK_ENTITY.subscribe { event ->
            if (event.hand == InteractionHand.MAIN_HAND) {
                val entity = event.entity as? NPCEntity ?: return@subscribe
                val player = event.player as? ServerPlayer ?: return@subscribe

                // Intercept execution chain via the unified Interaction Listener logic
                if (NPCInteractionListener.onInteract(player, entity)) {
                    event.cancel() // Prevent native processing from overriding custom behaviors
                }
            }
        }
    }
}