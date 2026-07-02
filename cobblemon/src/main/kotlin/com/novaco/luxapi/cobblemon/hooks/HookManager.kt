package com.novaco.luxapi.cobblemon.hooks

import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.pokemon.LevelUpEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionEvent
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * A centralized manager for registering and broadcasting custom game event hooks.
 * This registry allows external sub-modules (such as Bukkit/Paper bridges) to listen
 * to translated game activities smoothly.
 */
object HookManager {
    private val catchHooks = mutableListOf<Hook<Pokemon>>()
    private val defeatHooks = mutableListOf<Hook<BattleVictoryEvent>>()
    private val levelUpHooks = mutableListOf<Hook<LevelUpEvent>>()
    private val evolutionHooks = mutableListOf<Hook<EvolutionEvent>>()
    private val eggHatchHooks = mutableListOf<Hook<HatchEggEvent>>()

    /** Registers a hook to be triggered when a Pokémon is caught. */
    fun registerCatchHook(hook: Hook<Pokemon>) = catchHooks.add(hook)

    /** Registers a hook to be triggered when a player wins a battle. */
    fun registerDefeatHook(hook: Hook<BattleVictoryEvent>) = defeatHooks.add(hook)

    /** Registers a hook to be triggered when a Pokémon levels up. */
    fun registerLevelUpHook(hook: Hook<LevelUpEvent>) = levelUpHooks.add(hook)

    /** Registers a hook to be triggered when a Pokémon evolves. */
    fun registerEvolutionHook(hook: Hook<EvolutionEvent>) = evolutionHooks.add(hook)

    /** Registers a hook to be triggered when an egg hatches. */
    fun registerEggHatchHook(hook: Hook<HatchEggEvent>) = eggHatchHooks.add(hook)

    internal fun broadcastCatch(player: LuxPlayer, pokemon: Pokemon) {
        catchHooks.forEach { it.onTrigger(player, pokemon) }
    }

    internal fun broadcastDefeat(player: LuxPlayer, event: BattleVictoryEvent) {
        defeatHooks.forEach { it.onTrigger(player, event) }
    }

    internal fun broadcastLevelUp(player: LuxPlayer, event: LevelUpEvent) {
        levelUpHooks.forEach { it.onTrigger(player, event) }
    }

    internal fun broadcastEvolution(player: LuxPlayer, event: EvolutionEvent) {
        evolutionHooks.forEach { it.onTrigger(player, event) }
    }

    internal fun broadcastEggHatch(player: LuxPlayer, event: HatchEggEvent) {
        eggHatchHooks.forEach { it.onTrigger(player, event) }
    }
}