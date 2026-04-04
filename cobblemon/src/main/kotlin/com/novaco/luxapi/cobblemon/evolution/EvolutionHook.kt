package com.novaco.luxapi.cobblemon.evolution

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

/**
 * An abstract blueprint for defining custom, code-based evolution requirements.
 * Allows developers to enforce dynamic conditions that cannot be expressed in standard JSON.
 *
 * @property targetSpecies The name of the species this hook applies to (e.g., "pikachu").
 */
abstract class EvolutionHook(val targetSpecies: String) {

    /**
     * Evaluates the custom conditions required for the evolution to proceed.
     *
     * @param pokemon The Pokemon attempting to evolve.
     * @param player The owner of the Pokemon.
     * @return True if the evolution is allowed, false to cancel it.
     */
    abstract fun checkConditions(pokemon: Pokemon, player: ServerPlayer): Boolean

    /**
     * Triggered when the evolution is cancelled because the conditions were not met.
     * Useful for sending warning messages or hints to the player.
     *
     * @param pokemon The Pokemon that failed to evolve.
     * @param player The owner of the Pokemon.
     */
    open fun onFailure(pokemon: Pokemon, player: ServerPlayer) {}
}