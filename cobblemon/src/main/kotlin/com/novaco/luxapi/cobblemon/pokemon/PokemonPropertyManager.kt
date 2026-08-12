package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Safe, sync-aware mutation of a caught Pokémon's IVs/EVs/nature/held item.
 * Every setter here delegates to Cobblemon's own public mutation points
 * (the [Pokemon.ivs]/[Pokemon.evs] operator setters, the [Pokemon.nature] setter,
 * and [Pokemon.swapHeldItem]), so client sync and persistence-dirty marking are
 * handled by Cobblemon itself regardless of which store the Pokémon currently lives in.
 */
object PokemonPropertyManager {

    /**
     * Sets a single IV stat (0-31). Rejected out-of-range values are a silent no-op
     * on Cobblemon's side, so success is confirmed by reading the value back.
     */
    fun setIV(pokemon: Pokemon, stat: Stats, value: Int): Boolean {
        pokemon.ivs[stat] = value
        return pokemon.ivs.getOrDefault(stat) == value
    }

    /**
     * Sets a single EV stat (0-252 per stat, 510 total across all stats). Rejected
     * values are a silent no-op on Cobblemon's side, so success is confirmed by
     * reading the value back.
     */
    fun setEV(pokemon: Pokemon, stat: Stats, value: Int): Boolean {
        pokemon.evs[stat] = value
        return pokemon.evs.getOrDefault(stat) == value
    }
}
