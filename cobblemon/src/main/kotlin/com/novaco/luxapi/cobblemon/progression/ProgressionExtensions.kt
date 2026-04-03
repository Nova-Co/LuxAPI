package com.novaco.luxapi.cobblemon.progression

import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Extension functions providing rapid syntactic sugar for Pokédex and Progression features.
 * Allows developers to write clean, readable quest validation logic.
 */

/**
 * Checks if the player has caught the specified Pokémon.
 * Usage: if (player.hasCaught("mewtwo")) { ... }
 */
fun LuxPlayer.hasCaught(species: String): Boolean {
    return PokedexManager.hasCaught(this, species)
}

/**
 * Retrieves the total number of registered Pokémon in the player's Pokédex.
 * Usage: val total = player.getPokedexCount()
 */
fun LuxPlayer.getPokedexCount(): Int {
    return PokedexManager.getCaughtCount(this)
}

/**
 * Retrieves the player's current consecutive catch streak count.
 * Usage: val streak = player.getCatchStreakCount()
 */
fun LuxPlayer.getCatchStreakCount(): Int {
    return CatchStreakManager.getCurrentStreakCount(this)
}