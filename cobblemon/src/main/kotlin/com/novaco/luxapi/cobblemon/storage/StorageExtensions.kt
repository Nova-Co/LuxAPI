package com.novaco.luxapi.cobblemon.storage

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Extension functions mapping directly to the [PCStorageManager].
 * These functions provide a fluid, idiomatic Kotlin syntax for interacting
 * with a player's PC storage directly from the [LuxPlayer] object.
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

/**
 * Retrieves a statistical summary of the player's PC, including total count,
 * shinies, special rarities, and flawless Pokémon.
 */
fun LuxPlayer.getPCSummary(): PCSummary {
    return PCStorageManager.getSummary(this)
}

/**
 * Calculates and retrieves the average IV percentage of all Pokémon in the player's PC.
 */
fun LuxPlayer.getPCAverageIVs(): Double {
    return PCStorageManager.getStats(this)
}

/**
 * Organizes the player's PC by moving or swapping a Pokémon between specific slots.
 * * @param fromBox Source box index (0-based).
 * @param fromSlot Source slot index (0-based).
 * @param toBox Destination box index (0-based).
 * @param toSlot Destination slot index (0-based).
 * @return True if the move was successful, false if out of bounds.
 */
fun LuxPlayer.movePokemonInPC(fromBox: Int, fromSlot: Int, toBox: Int, toSlot: Int): Boolean {
    return PCStorageManager.move(this, fromBox, fromSlot, toBox, toSlot)
}