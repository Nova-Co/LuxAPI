package com.novaco.luxapi.cobblemon.npc.extensions

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.cobblemon.npc.LuxNPCBuilder
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Extension functions to streamline the creation and spawning of universal NPCs.
 * Provides a highly readable Kotlin DSL.
 */

/**
 * Opens a builder context to configure and instantly spawn a universal NPC
 * in front of the player.
 *
 * Example Usage:
 * ```
 * player.spawnNPC {
 * name("§bProfessor Oak")
 * skin("Prof_Oak")
 * movement(LuxMovement.STATIONARY)
 * onInteract { p, npc ->
 * // Handle dialogue logic here
 * }
 * }
 * ```
 *
 * @param block The configuration block applied to the [LuxNPCBuilder].
 * @return The generated [NPCEntity], or null if spawning failed.
 */
inline fun LuxPlayer.spawnNPC(block: LuxNPCBuilder.() -> Unit): NPCEntity? {
    val builder = LuxNPCBuilder(this)
    builder.block()
    return builder.spawn()
}