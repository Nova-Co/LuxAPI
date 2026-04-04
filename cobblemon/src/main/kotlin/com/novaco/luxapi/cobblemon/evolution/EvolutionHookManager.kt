package com.novaco.luxapi.cobblemon.evolution

import com.cobblemon.mod.common.api.events.CobblemonEvents

/**
 * A central registry for intercepting and enforcing custom rules on Pokemon evolutions.
 * Listens to native Cobblemon events and applies registered hooks seamlessly.
 */
object EvolutionHookManager {

    private val hooks = mutableListOf<EvolutionHook>()

    /**
     * Registers a new custom evolution hook into the system.
     *
     * @param hook The hook implementation containing the custom conditions.
     */
    fun register(hook: EvolutionHook) {
        hooks.add(hook)
    }

    /**
     * Initializes the evolution event listener.
     * This must be called once during the platform initialization phase (e.g., NeoForge setup).
     */
    fun initialize() {
        CobblemonEvents.EVOLUTION_ACCEPTED.subscribe { event ->
            val pokemon = event.pokemon
            val player = pokemon.getOwnerPlayer()

            if (player != null) {
                for (hook in hooks) {
                    if (hook.targetSpecies.equals(pokemon.species.name, ignoreCase = true)) {
                        val canEvolve = hook.checkConditions(pokemon, player)

                        if (!canEvolve) {
                            event.cancel()
                            hook.onFailure(pokemon, player)
                        }
                    }
                }
            }
        }
    }
}