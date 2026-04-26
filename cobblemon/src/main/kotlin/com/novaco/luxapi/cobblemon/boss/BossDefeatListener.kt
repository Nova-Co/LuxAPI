package com.novaco.luxapi.cobblemon.boss

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.aggro.BossAggroManager
import com.novaco.luxapi.cobblemon.boss.event.BossDefeatEvent
import com.novaco.luxapi.cobblemon.boss.event.LuxBossHooks
import com.novaco.luxapi.cobblemon.boss.minion.BossMinionManager
import com.novaco.luxapi.core.bossbar.BossBarManager
import com.novaco.luxapi.core.scoreboard.ScoreboardManager
import net.minecraft.server.level.ServerPlayer

/**
 * Core listener to handle the final stage of a Boss Pokémon's lifecycle.
 */
object BossDefeatListener {

    private val defeatHooks = mutableListOf<(PokemonEntity, List<ServerPlayer>, PokemonBattle) -> Unit>()

    /**
     * Registers a custom callback to be executed when a boss is defeated.
     */
    fun onBossDefeated(action: (PokemonEntity, List<ServerPlayer>, PokemonBattle) -> Unit) {
        defeatHooks.add(action)
    }

    /**
     * Initializes the event subscriber for battle victories.
     */
    fun register() {
        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val battle = event.battle
            val winners = event.winners

            val bossActor = battle.actors.find { actor ->
                actor.pokemonList.any { pkmn ->
                    pkmn.entity?.tags?.contains("lux_is_boss") == true || pkmn.entity?.tags?.contains("lux_is_world_boss") == true
                }
            } ?: return@subscribe

            if (!winners.contains(bossActor)) {
                val bossBattlePokemon = bossActor.pokemonList.firstOrNull { pkmn ->
                    pkmn.entity?.tags?.contains("lux_is_boss") == true || pkmn.entity?.tags?.contains("lux_is_world_boss") == true
                }

                val bossEntity = bossBattlePokemon?.entity ?: return@subscribe

                val participatingPlayers = battle.actors
                    .filterIsInstance<PlayerBattleActor>()
                    .mapNotNull { it.entity }

                handleDefeatSequence(bossEntity, participatingPlayers, battle)
            }
        }
    }

    /**
     * Executes the cleanup logic and fires all registered developer hooks.
     */
    private fun handleDefeatSequence(bossEntity: PokemonEntity, players: List<ServerPlayer>, battle: PokemonBattle) {
        defeatHooks.forEach { hook ->
            try {
                hook(bossEntity, players, battle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        BossBarManager.unregister(bossEntity.uuid)
        BossHpSynchronizer.unbind(bossEntity.uuid)

        val server = bossEntity.server
        if (server != null) {
            ScoreboardManager.destroyScoreboard("raid_${bossEntity.uuid}") { uuid ->
                server.playerList.getPlayer(uuid)
            }
        }

        BossMinionManager.clearMinions(bossEntity.uuid)

        val topDamagers = BossAggroManager.getTopDamagers(bossEntity)
        LuxBossHooks.triggerDefeat(BossDefeatEvent(bossEntity, topDamagers))

        bossEntity.discard()
    }
}