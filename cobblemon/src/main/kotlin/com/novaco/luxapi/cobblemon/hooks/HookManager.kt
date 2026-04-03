package com.novaco.luxapi.cobblemon.hooks

import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.pokemon.LevelUpEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionEvent
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer

object HookManager {
    private val catchHooks = mutableListOf<LuxHook<Pokemon>>()
    private val defeatHooks = mutableListOf<LuxHook<BattleVictoryEvent>>()
    private val levelUpHooks = mutableListOf<LuxHook<LevelUpEvent>>()
    private val evolutionHooks = mutableListOf<LuxHook<EvolutionEvent>>()
    private val eggHatchHooks = mutableListOf<LuxHook<HatchEggEvent>>()

    fun registerCatchHook(hook: LuxHook<Pokemon>) = catchHooks.add(hook)
    fun registerDefeatHook(hook: LuxHook<BattleVictoryEvent>) = defeatHooks.add(hook)
    fun registerLevelUpHook(hook: LuxHook<LevelUpEvent>) = levelUpHooks.add(hook)
    fun registerEvolutionHook(hook: LuxHook<EvolutionEvent>) = evolutionHooks.add(hook)
    fun registerEggHatchHook(hook: LuxHook<HatchEggEvent>) = eggHatchHooks.add(hook)

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