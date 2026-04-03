package com.novaco.luxapi.cobblemon.storage

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Extension functions mapping directly to the [PCStorageManager],
 * providing fluid syntax for PC interactions.
 */

/**
 * Searches the player's PC for Pokémon matching the specific condition.
 * Usage: val charizards = player.findInPC { it.species.name == "charizard" }
 */
fun LuxPlayer.findInPC(predicate: (Pokemon) -> Boolean): List<Pokemon> {
    return PCStorageManager.find(this, predicate)
}

/**
 * Retrieves all shiny Pokémon currently stored in the player's PC.
 */
fun LuxPlayer.getShinyPokemonInPC(): List<Pokemon> {
    return PCStorageManager.getShinies(this)
}

/**
 * Retrieves all Legendary, Mythical, and Ultra Beasts from the player's PC.
 */
fun LuxPlayer.getLegendariesInPC(): List<Pokemon> {
    return PCStorageManager.getSpecialRarity(this)
}

/**
 * Permanently releases all Pokémon from the PC that match the condition.
 * Usage: val deleted = player.massReleaseFromPC { it.level < 10 }
 * * @return The number of Pokémon deleted.
 */
fun LuxPlayer.massReleaseFromPC(predicate: (Pokemon) -> Boolean): Int {
    return PCStorageManager.massRelease(this, predicate)
}