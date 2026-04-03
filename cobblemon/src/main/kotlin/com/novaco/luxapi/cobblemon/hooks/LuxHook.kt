package com.novaco.luxapi.cobblemon.hooks

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Basic interface for all quest-related objectives.
 * Other developers will implement this to receive updates from the game.
 */
interface LuxHook<T> {
    fun onTrigger(player: LuxPlayer, data: T)
}