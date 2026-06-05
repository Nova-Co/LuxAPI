package com.novaco.luxapi.cobblemon.npc.extensions

import com.cobblemon.mod.common.battles.BattleBuilder
import com.cobblemon.mod.common.battles.ErroredBattleStart
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.cobblemon.npc.LuxNPCBuilder
import com.novaco.luxapi.cobblemon.npc.manager.LuxNPCManager
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Extension functions to streamline the creation, management, and interactions
 * of universal NPCs within the LuxAPI ecosystem.
 */

/**
 * Opens a builder context to configure and instantly spawn a universal NPC
 * in front of the player.
 *
 * @param block The configuration block applied to the [LuxNPCBuilder].
 * @return The generated [NPCEntity], or null if spawning failed.
 */
inline fun LuxPlayer.spawnNPC(block: LuxNPCBuilder.() -> Unit): NPCEntity? {
    val builder = LuxNPCBuilder(this)
    builder.block()
    return builder.spawn()
}

/**
 * Instantly initiates a PvN (Player vs NPC) battle.
 * Utilizes Cobblemon's native BattleBuilder to construct the encounter.
 * Automatically alerts the player if the battle cannot start (e.g., all Pokémon fainted).
 *
 * @param player The player challenging the NPC.
 */
fun NPCEntity.initiateBattle(player: ServerPlayer) {
    val result = BattleBuilder.pvn(
        player = player,
        npcEntity = this
    )

    if (result is ErroredBattleStart) {
        result.sendTo(player)
    }
}