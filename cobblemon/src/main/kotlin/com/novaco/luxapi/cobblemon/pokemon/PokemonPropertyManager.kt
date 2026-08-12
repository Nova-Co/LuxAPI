package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.item.ItemStack

/**
 * Safe, sync-aware mutation of a caught Pokémon's IVs/EVs/nature/held item.
 * Every setter here delegates to Cobblemon's own public mutation points
 * ([Pokemon.setIV]/[Pokemon.setEV], the [Pokemon.nature] setter, and
 * [Pokemon.swapHeldItem]), so client sync, persistence-dirty marking, and (for
 * IV/EV) the max-HP recalculation when the HP stat itself changes are all
 * handled by Cobblemon itself, regardless of which store the Pokémon currently lives in.
 */
object PokemonPropertyManager {

    /**
     * Sets a single IV stat (0-31) via Cobblemon's own [Pokemon.setIV] (which also
     * recalculates max HP if [stat] is HP). Rejected out-of-range values are a
     * silent no-op on Cobblemon's side, so success is confirmed by reading the
     * value back.
     */
    fun setIV(pokemon: Pokemon, stat: Stats, value: Int): Boolean {
        pokemon.setIV(stat, value)
        return pokemon.ivs.getOrDefault(stat) == value
    }

    /**
     * Sets a single EV stat (0-252 per stat, 510 total across all stats) via
     * Cobblemon's own [Pokemon.setEV] (which also recalculates max HP if [stat]
     * is HP). Rejected values are a silent no-op on Cobblemon's side, so success
     * is confirmed by reading the value back.
     */
    fun setEV(pokemon: Pokemon, stat: Stats, value: Int): Boolean {
        pokemon.setEV(stat, value)
        return pokemon.evs.getOrDefault(stat) == value
    }

    /**
     * Looks up a nature by its Cobblemon identifier (e.g. "adamant") and assigns it.
     * Returns false without touching the Pokémon if the id doesn't resolve to a
     * known nature.
     */
    fun setNature(pokemon: Pokemon, natureId: String): Boolean {
        val nature = Natures.getNature(natureId) ?: return false
        pokemon.nature = nature
        return true
    }

    /**
     * Assigns [item] as the Pokémon's held item, returning whatever it was
     * previously holding. Unlike [Pokemon.swapHeldItem]'s own default, this
     * defaults [decrement] to false: callers here are typically constructing an
     * [ItemStack] value to assign as a property, not handing over a live stack
     * from a player's inventory that should be consumed by 1.
     */
    fun setHeldItem(pokemon: Pokemon, item: ItemStack, decrement: Boolean = false): ItemStack {
        return pokemon.swapHeldItem(item, decrement, aiCanDrop = true)
    }
}
