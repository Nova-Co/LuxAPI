package com.novaco.luxapi.cobblemon.gui

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.novaco.luxapi.commons.gui.GuiItem

/**
 * Builds [GuiItem]s that render a real, live-species Cobblemon Pokemon model in a GUI slot
 * instead of a flat icon standing in for it.
 */
object PokemonGuiItem {

    /**
     * From a live Pokemon (a boss's `entity.pokemon`, a party member, a PC box entry, etc.).
     */
    fun of(pokemon: Pokemon, displayName: String = "", lore: List<String> = emptyList()): GuiItem =
        GuiItem(PokemonItem.from(pokemon, 1), displayName, lore)

    /**
     * From just a species (+ optional aspects, e.g. "shiny") — no live Pokemon object needed, for
     * pickers/menus that only know a species ahead of time (e.g. a boss-profile spawn picker).
     */
    fun of(species: Species, aspects: Set<String> = emptySet(), displayName: String = "", lore: List<String> = emptyList()): GuiItem {
        val spec = buildString {
            append("species=${species.name}")
            aspects.forEach { append(" aspect=$it") }
        }
        return of(PokemonProperties.parse(spec).create(), displayName, lore)
    }
}
