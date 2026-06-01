package com.novaco.luxapi.cobblemon.hooks

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * A generic interface for creating custom hooks that respond to specific in-game events.
 * This serves as a standardized way for developers to implement listeners for events
 * managed by the [HookManager], such as quest objectives or custom triggers.
 *
 * @param T The type of data that will be passed when the event is triggered.
 */
interface LuxHook<T> {
    /**
     * This method is called by the [HookManager] when the corresponding event occurs.
     *
     * @param player The player associated with the event.
     * @param data The event-specific data (e.g., the Pokémon that was caught, details of a battle).
     */
    fun onTrigger(player: LuxPlayer, data: T)
}