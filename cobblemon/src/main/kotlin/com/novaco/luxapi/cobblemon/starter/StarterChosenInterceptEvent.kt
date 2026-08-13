package com.novaco.luxapi.cobblemon.starter

import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

/**
 * A simplified view of a Cobblemon [StarterChosenEvent], as delivered by
 * [StarterInterceptor.onStarterChosen].
 *
 * @property player The player who chose the starter.
 * @property properties The [PokemonProperties] string the choice was resolved from.
 * @property pokemon The [Pokemon] about to be given to [player].
 */
data class StarterChosenInterceptEvent(
    val player: ServerPlayer,
    val properties: PokemonProperties,
    val pokemon: Pokemon,
    private val source: StarterChosenEvent
) {
    /** Prevents this starter from being given to [player] at all. */
    fun cancel() {
        source.cancel()
    }

    /**
     * Replaces the Pokémon [player] will actually receive, overriding their choice.
     * Cobblemon's own [StarterChosenEvent.pokemon] is a `var` specifically to support this.
     */
    fun replacePokemon(newPokemon: Pokemon) {
        source.pokemon = newPokemon
    }
}
