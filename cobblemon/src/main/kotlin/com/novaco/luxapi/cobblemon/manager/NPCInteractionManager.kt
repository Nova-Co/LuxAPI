package com.novaco.luxapi.cobblemon.manager

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.platform.events.PlatformEvents
import net.minecraft.world.InteractionHand

/**
 * Manages global, ambient interactions for Cobblemon NPCs across all platforms.
 * This object handles behaviors that are automatically applied based on NPC properties or tags,
 * such as making an NPC look at a nearby player.
 */
object NPCInteractionManager {

    /**
     * Registers the necessary platform event listeners to enable managed NPC interactions.
     * This includes:
     * - A server tick listener to handle continuous behaviors like looking at players.
     * - A right-click listener to intercept interactions for future features like auto-dialogue.
     */
    fun register() {
        // On every server tick, find NPCs that should be looking at players.
        PlatformEvents.SERVER_TICK_POST.subscribe { event ->
            val server = event.server

            for (level in server.allLevels) {
                for (player in level.players()) {
                    if (!player.isAlive || player.isSpectator) continue

                    // Define a search area around the player.
                    val searchBox = player.boundingBox.inflate(7.0, 3.0, 7.0)

                    // Find all NPCs within that area that have the specific tag and are not in battle.
                    val nearbyNpcs = level.getEntitiesOfClass(NPCEntity::class.java, searchBox) { npc ->
                        npc.isAlive && npc.tags.contains("lux_look_at_interactor") && !npc.isInBattle()
                    }

                    // Make each nearby NPC look towards the player.
                    for (npc in nearbyNpcs) {
                        npc.lookControl.setLookAt(player, 30.0f, 30.0f)
                        npc.yBodyRot = npc.yHeadRot // Sync body rotation with head rotation for a more natural look.
                    }
                }
            }
        }

        // Listen for when a player right-clicks an entity.
        PlatformEvents.RIGHT_CLICK_ENTITY.subscribe { event ->
            if (event.hand == InteractionHand.MAIN_HAND) {
                val entity = event.entity

                if (entity is NPCEntity) {
                    // TODO: Implement logic for handling right-clicks, such as triggering
                    // a pre-configured dialogue associated with the NPC.
                }
            }
        }
    }
}