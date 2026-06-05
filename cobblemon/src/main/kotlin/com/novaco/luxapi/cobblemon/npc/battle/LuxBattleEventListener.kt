package com.novaco.luxapi.cobblemon.npc.battle

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.entity.npc.NPCBattleActor
import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.server.level.ServerPlayer

/**
 * Listens for Cobblemon battle termination events and routes them to the LuxBattleRegistry.
 */
object LuxBattleEventListener {

    /**
     * Subscribes to the Cobblemon event bus.
     */
    fun register() {
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL) { event ->
            handleBattleEnd(event.winners.toList(), event.losers.toList(), LuxBattleResult.VICTORY)
        }

        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL) { event ->
            val battle = event.battle
            handleBattleEnd(battle.actors.toList(), battle.actors.toList(), LuxBattleResult.DEFEAT)
        }
    }

    /**
     * Processes the battle actors and triggers the associated registry callbacks.
     *
     * @param winners The list of winning battle actors.
     * @param losers The list of losing battle actors.
     * @param defaultResult The presumed outcome to evaluate against the player.
     */
    private fun handleBattleEnd(
        winners: List<Any>,
        losers: List<Any>,
        defaultResult: LuxBattleResult
    ) {
        val allActors = winners + losers

        val playerActor = allActors.filterIsInstance<PlayerBattleActor>().firstOrNull() ?: return
        val npcActor = allActors.filterIsInstance<NPCBattleActor>().firstOrNull() ?: return

        val player = playerActor.entity as? ServerPlayer ?: return
        val npc = npcActor.entity as? NPCEntity ?: return

        val result = if (winners.contains(playerActor)) {
            LuxBattleResult.VICTORY
        } else if (losers.contains(playerActor)) {
            LuxBattleResult.DEFEAT
        } else {
            LuxBattleResult.DRAW
        }

        LuxBattleRegistry.get(npc.uuid)?.invoke(player, npc, result)
    }
}