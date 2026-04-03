package com.novaco.luxapi.cobblemon.storage

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.pokemon.getIVPercentage
import com.novaco.luxapi.cobblemon.util.PokemonInfoUtils
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

/**
 * An advanced management engine for handling the player's PC storage.
 * This provides highly optimized search, filter, and mass-action capabilities,
 * eliminating the need for developers to write boilerplate iteration loops.
 */
object PCStorageManager {

    /**
     * Safely retrieves the player's complete PC store from the Cobblemon Storage API.
     *
     * @param player The target player.
     * @return The [PCStore] instance associated with the player.
     */
    fun getPC(player: LuxPlayer): PCStore {
        val serverPlayer = player.parent as ServerPlayer
        return Cobblemon.storage.getPC(serverPlayer)
    }

    /**
     * Scans the entire PC storage and returns a list of Pokémon that match the given predicate.
     *
     * @param player The target player.
     * @param predicate A lambda function defining the search condition.
     * @return A list of [Pokemon] matching the exact criteria.
     */
    fun find(player: LuxPlayer, predicate: (Pokemon) -> Boolean): List<Pokemon> {
        val pc = getPC(player)
        val results = mutableListOf<Pokemon>()

        for (pokemon in pc) {
            if (pokemon != null && predicate(pokemon)) {
                results.add(pokemon)
            }
        }

        return results
    }

    /**
     * Executes a mass-release operation, permanently deleting all Pokémon from the PC
     * that match the given condition. This is highly useful for "Trash Bins" or
     * automatic inventory management systems.
     *
     * @param player The target player.
     * @param predicate A lambda function defining which Pokémon should be released.
     * @return The total number of Pokémon that were successfully released.
     */
    fun massRelease(player: LuxPlayer, predicate: (Pokemon) -> Boolean): Int {
        val pc = getPC(player)
        val toRelease = mutableListOf<Pokemon>()

        for (pokemon in pc) {
            if (pokemon != null && predicate(pokemon)) {
                toRelease.add(pokemon)
            }
        }

        for (pokemon in toRelease) {
            pc.remove(pokemon)
        }

        return toRelease.size
    }

    /**
     * Pre-built filter: Retrieves all Shiny Pokémon stored in the PC.
     *
     * @param player The target player.
     * @return A list of shiny [Pokemon].
     */
    fun getShinies(player: LuxPlayer): List<Pokemon> {
        return find(player) { it.shiny }
    }

    /**
     * Pre-built filter: Retrieves all Legendary, Mythical, or Ultra Beast Pokémon.
     * Relies on the previously established PokemonInfoUtils.
     *
     * @param player The target player.
     * @return A list of special rarity [Pokemon].
     */
    fun getSpecialRarity(player: LuxPlayer): List<Pokemon> {
        return find(player) { PokemonInfoUtils.isSpecialRarity(it) }
    }

    /**
     * Pre-built filter: Retrieves all flawless Pokémon (100% IVs).
     *
     * @param player The target player.
     * @return A list of flawless [Pokemon].
     */
    fun getFlawless(player: LuxPlayer): List<Pokemon> {
        return find(player) { it.getIVPercentage() >= 100.0 }
    }
}