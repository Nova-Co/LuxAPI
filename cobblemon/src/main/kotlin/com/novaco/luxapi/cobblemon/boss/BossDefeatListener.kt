package com.novaco.luxapi.cobblemon.boss

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.aggro.BossAggroManager
import com.novaco.luxapi.cobblemon.boss.event.BossDefeatEvent
import com.novaco.luxapi.cobblemon.boss.event.BossHooks
import com.novaco.luxapi.cobblemon.boss.minion.BossMinionManager
import com.novaco.luxapi.core.bossbar.BossBarManager
import com.novaco.luxapi.core.scoreboard.ScoreboardManager
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * Handles the final stage of a boss's lifecycle: its defeat.
 * This listener is responsible for detecting when a boss has been defeated in battle,
 * executing cleanup logic, and firing relevant events and developer hooks.
 */
object BossDefeatListener {

    private val defeatHooks = mutableListOf<(PokemonEntity, List<ServerPlayer>, PokemonBattle) -> Unit>()

    /**
     * Boss actor/entity captured at [CobblemonEvents.BATTLE_STARTED_POST] time, keyed by battle ID.
     * `BattlePokemon.entity` (a live-lookup through the `Pokemon` data object) goes stale by the time
     * `BATTLE_VICTORY` posts, since `PokemonBattle.end()` runs synchronously beforehand and detaches
     * the entity from its `Pokemon` data during the heal/recall pass. Snapshotting here, while the
     * entity is still attached, avoids re-deriving it from a reference that's no longer reliable.
     */
    private val trackedBossBattles = mutableMapOf<UUID, Pair<BattleActor, PokemonEntity>>()

    /**
     * Registers a custom callback to be executed when a boss is defeated.
     * This allows other developers to add their own logic, such as loot drops or announcements.
     *
     * @param action The lambda to execute, providing the defeated boss, participating players, and the battle context.
     */
    fun onBossDefeated(action: (PokemonEntity, List<ServerPlayer>, PokemonBattle) -> Unit) {
        defeatHooks.add(action)
    }

    /**
     * Initializes the listener by subscribing to Cobblemon's battle victory event.
     * It filters these events to identify when a battle involving a LuxAPI boss has concluded with the boss's loss.
     */
    fun register() {
        CobblemonEvents.BATTLE_STARTED_POST.subscribe { event ->
            val battle = event.battle

            // Entities are still attached to their Pokemon data here, so this lookup is reliable.
            val bossActor = battle.actors.find { actor ->
                actor.pokemonList.any { pkmn ->
                    pkmn.entity?.tags?.contains("lux_is_boss") == true || pkmn.entity?.tags?.contains("lux_is_world_boss") == true
                }
            } ?: return@subscribe

            val bossEntity = bossActor.pokemonList.firstNotNullOfOrNull { pkmn ->
                pkmn.entity?.takeIf { it.tags.contains("lux_is_boss") || it.tags.contains("lux_is_world_boss") }
            } ?: return@subscribe

            trackedBossBattles[battle.battleId] = bossActor to bossEntity
        }

        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val battle = event.battle
            val winners = event.winners

            val (bossActor, bossEntity) = trackedBossBattles.remove(battle.battleId) ?: return@subscribe

            // Check if the boss was on the losing side
            if (!winners.contains(bossActor)) {
                val participatingPlayers = battle.actors
                    .filterIsInstance<PlayerBattleActor>()
                    .mapNotNull { it.entity }

                handleDefeatSequence(bossEntity, participatingPlayers, battle)
            }
        }
    }

    /**
     * Orchestrates the entire cleanup sequence after a boss is confirmed to be defeated.
     * This includes running custom hooks, cleaning up UI, removing minions, and firing the final event.
     *
     * @param bossEntity The PokemonEntity of the defeated boss.
     * @param players A list of players who participated in the battle.
     * @param battle The final battle object for context.
     */
    private fun handleDefeatSequence(bossEntity: PokemonEntity, players: List<ServerPlayer>, battle: PokemonBattle) {
        // Execute all registered custom developer logic
        defeatHooks.forEach { hook ->
            try {
                hook(bossEntity, players, battle)
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }

        // Clean up associated UI and tracking elements
        BossBarManager.unregister(bossEntity.uuid)
        BossHpSynchronizer.unbind(bossEntity.uuid)

        // Destroy the raid scoreboard
        val server = bossEntity.server
        if (server != null) {
            ScoreboardManager.destroyScoreboard("raid_${bossEntity.uuid}") { uuid ->
                server.playerList.getPlayer(uuid)
            }
        }

        // Remove any active minions
        BossMinionManager.clearMinions(bossEntity.uuid)

        // Fire the official API event with damage data
        val topDamagers = BossAggroManager.getTopDamagers(bossEntity)
        BossHooks.triggerDefeat(BossDefeatEvent(bossEntity, topDamagers))

        // Despawn the boss entity
        bossEntity.discard()
    }
}