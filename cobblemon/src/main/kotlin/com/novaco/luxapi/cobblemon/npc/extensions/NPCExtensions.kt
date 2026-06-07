package com.novaco.luxapi.cobblemon.npc.extensions

import com.cobblemon.mod.common.battles.BattleBuilder
import com.cobblemon.mod.common.battles.BattleStartResult
import com.cobblemon.mod.common.battles.ErroredBattleStart
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.cobblemon.npc.NPCBuilder
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

/**
 * Extension functions to streamline the creation, management, and interactions
 * of universal NPCs within the LuxAPI ecosystem.
 */

/**
 * Opens a builder context to configure and instantly spawn a universal NPC
 * in front of the player.
 *
 * @param block The configuration block applied to the [NPCBuilder].
 * @return The generated [NPCEntity], or null if spawning failed.
 */
inline fun LuxPlayer.spawnNPC(block: NPCBuilder.() -> Unit): NPCEntity? {
    val builder = NPCBuilder(this)
    builder.block()
    return builder.spawn()
}

/**
 * Instantly initiates a PvN (Player vs NPC) battle.
 * Utilizes Cobblemon's native BattleBuilder to construct the encounter.
 * * @param player The player challenging the NPC.
 * @param onError Optional callback giving developers full control over how to handle custom messaging
 * or side-effects when a battle fails to start. If not provided, it falls back to native messaging.
 * @return The final [BattleStartResult] so developers can do post-processing if needed.
 */
fun NPCEntity.initiateBattle(
    player: ServerPlayer,
    onError: ((ServerPlayer, ErroredBattleStart) -> Unit)? = { p, result -> result.sendTo(p) }
): BattleStartResult {
    val result = BattleBuilder.pvn(
        player = player,
        npcEntity = this
    )

    if (result is ErroredBattleStart) {
        onError?.invoke(player, result)
    }

    return result
}