package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.scheduler.LuxTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * A one-shot action tied to a specific Pokemon's health ratio dropping to or below [healthThreshold]
 * during a battle. [isTriggered] ensures it only fires once, same semantics as
 * [com.novaco.luxapi.cobblemon.boss.phase.BossPhase].
 */
data class BattleHealthTrigger(
    val targetUuid: UUID,
    val healthThreshold: Float,
    val action: (PokemonBattle, BattlePokemon) -> Unit,
    var isTriggered: Boolean = false
)

/**
 * Health-threshold triggers and forced battle termination for in-progress Cobblemon battles.
 * Cobblemon has no HP-changed event for in-battle Pokemon, so thresholds are evaluated by polling
 * once per second via LuxAPI's scheduler for the lifetime of each battle that has active triggers.
 */
object BattleInterceptor {

    private val activeTriggers = ConcurrentHashMap<UUID, MutableList<BattleHealthTrigger>>()
    private val pollTasks = ConcurrentHashMap<UUID, LuxTask>()

    /**
     * Registers a one-shot action that fires when [target]'s health ratio drops to or below
     * [threshold] (0.0-1.0) during [battle]. Starts a per-battle poll task on first use.
     */
    fun onHealthThreshold(
        battle: PokemonBattle,
        target: BattlePokemon,
        threshold: Float,
        action: (PokemonBattle, BattlePokemon) -> Unit
    ) {
        val triggers = activeTriggers.getOrPut(battle.battleId) { mutableListOf() }
        triggers.add(BattleHealthTrigger(target.originalPokemon.uuid, threshold, action))

        pollTasks.getOrPut(battle.battleId) {
            LuxAPI.getScheduler().runRepeating(20L, 20L) {
                evaluate(battle)
            }
        }
    }

    /** Forcefully ends the battle via Cobblemon's own teardown (fainted/evolution bookkeeping included). */
    fun stopBattle(battle: PokemonBattle) {
        battle.stop()
    }

    /** Stops this battle's poll task (if any) and clears its registered triggers. Call on battle end. */
    fun unregister(battle: PokemonBattle) {
        pollTasks.remove(battle.battleId)?.cancel()
        activeTriggers.remove(battle.battleId)
    }

    private fun evaluate(battle: PokemonBattle) {
        val triggers = activeTriggers[battle.battleId] ?: return

        for (activePokemon in battle.activePokemon) {
            val battlePokemon = activePokemon.battlePokemon ?: continue
            val pokemon = battlePokemon.originalPokemon
            val maxHealth = pokemon.maxHealth
            if (maxHealth <= 0) continue
            val ratio = pokemon.currentHealth.toFloat() / maxHealth.toFloat()

            for (trigger in triggers) {
                if (!trigger.isTriggered && trigger.targetUuid == pokemon.uuid && ratio <= trigger.healthThreshold) {
                    trigger.isTriggered = true
                    trigger.action(battle, battlePokemon)
                }
            }
        }
    }
}
