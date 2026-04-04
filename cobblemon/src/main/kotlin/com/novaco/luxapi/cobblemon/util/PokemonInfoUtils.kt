package com.novaco.luxapi.cobblemon.util

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Advanced utility functions for extracting deep information from a Pokémon.
 * This class provides formatted data outputs commonly required for UI displays,
 * holograms, or chat announcements.
 *
 */
object PokemonInfoUtils {

    /**
     * Formats the Pokémon's elemental types into a clean, readable string.
     * Handles both single-type and dual-type Pokémon.
     *
     * @param pokemon The target [Pokemon].
     * @param separator The string used to divide dual types (default is " / ").
     * @return A formatted string, e.g., "Fire / Flying" or "Water".
     */
    fun getFormattedTyping(pokemon: Pokemon, separator: String = " / "): String {
        val primaryType = pokemon.form.primaryType.name.replaceFirstChar { it.uppercase() }
        val secondaryType = pokemon.form.secondaryType?.name?.replaceFirstChar { it.uppercase() }

        return if (secondaryType != null) {
            "$primaryType$separator$secondaryType"
        } else {
            primaryType
        }
    }

    /**
     * Checks if the Pokémon belongs to any special rarity tier
     * (Legendary, Mythical, or Ultra Beast).
     *
     * @param pokemon The target [Pokemon].
     * @return True if the Pokémon has a special rarity label, false otherwise.
     */
    fun isSpecialRarity(pokemon: Pokemon): Boolean {
        val labels = pokemon.form.labels
        return labels.contains("legendary") ||
                labels.contains("mythical") ||
                labels.contains("ultra_beast")
    }

    /**
     * Generates a summarized text displaying the Pokémon's basic combat stats (EVs).
     * Useful for player inspection menus.
     *
     * @param pokemon The target [Pokemon].
     * @return A formatted string displaying all EVs.
     */
    fun getEvSummary(pokemon: Pokemon): String {
        val evs = pokemon.evs
        return "HP: ${evs.getOrDefault(com.cobblemon.mod.common.api.pokemon.stats.Stats.HP)} | " +
                "ATK: ${evs.getOrDefault(com.cobblemon.mod.common.api.pokemon.stats.Stats.ATTACK)} | " +
                "DEF: ${evs.getOrDefault(com.cobblemon.mod.common.api.pokemon.stats.Stats.DEFENCE)} | " +
                "SP.ATK: ${evs.getOrDefault(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_ATTACK)} | " +
                "SP.DEF: ${evs.getOrDefault(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPECIAL_DEFENCE)} | " +
                "SPD: ${evs.getOrDefault(com.cobblemon.mod.common.api.pokemon.stats.Stats.SPEED)}"
    }
}