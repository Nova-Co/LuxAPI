package com.novaco.luxapi.cobblemon.storage

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.pokemon.getIVPercentage
import com.novaco.luxapi.cobblemon.util.PokemonInfoUtils
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

/**
 * A data representation holding summarized statistics of a player's PC.
 * Highly useful for building UI dashboards or player stat checking.
 */
data class PCSummary(
    val totalPokemon: Int,
    val shinyCount: Int,
    val specialRarityCount: Int,
    val flawlessCount: Int
)

/**
 * An advanced management engine for handling the player's PC storage.
 * This provides highly optimized search, filter, summary, and mass-action capabilities,
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

    /**
     * Scans the PC in a single optimized loop and generates a summary of its contents.
     * Perfect for rendering player profile menus or leaderboards.
     *
     * @param player The target player.
     * @return A [PCSummary] data object containing the aggregated statistics.
     */
    fun getSummary(player: LuxPlayer): PCSummary {
        var total = 0
        var shinies = 0
        var specials = 0
        var flawless = 0

        for (pokemon in getPC(player)) {
            if (pokemon == null) continue

            total++
            if (pokemon.shiny) shinies++
            if (PokemonInfoUtils.isSpecialRarity(pokemon)) specials++
            if (pokemon.getIVPercentage() >= 100.0) flawless++
        }

        return PCSummary(total, shinies, specials, flawless)
    }

    /**
     * Calculates the overall average IV percentage of all Pokémon stored in the PC.
     * Useful for administrative checks or server-wide power scaling.
     *
     * @param player The target player.
     * @return The average IV percentage (0.0 to 100.0). Returns 0.0 if the PC is empty.
     */
    fun getStats(player: LuxPlayer): Double {
        var totalPercentage = 0.0
        var count = 0

        for (pokemon in getPC(player)) {
            if (pokemon == null) continue
            totalPercentage += pokemon.getIVPercentage()
            count++
        }

        return if (count == 0) 0.0 else totalPercentage / count
    }

    /**
     * Moves (or swaps) a Pokémon between specific coordinates within the PC.
     * If the target slot is already occupied, the Pokémon will swap places.
     *
     * @param player The target player.
     * @param fromBox The zero-based index of the source box.
     * @param fromSlot The zero-based index of the slot within the source box (0-29).
     * @param toBox The zero-based index of the destination box.
     * @param toSlot The zero-based index of the slot within the destination box (0-29).
     * @return True if the move was successful, false if the coordinates were out of bounds.
     */
    fun move(player: LuxPlayer, fromBox: Int, fromSlot: Int, toBox: Int, toSlot: Int): Boolean {
        val pc = getPC(player)
        val boxes = pc.boxes

        // Validate box boundaries
        if (fromBox !in boxes.indices || toBox !in boxes.indices) return false

        // Validate slot boundaries (Cobblemon strictly uses 30 slots per box)
        val slotsPerBox = 30
        if (fromSlot !in 0 until slotsPerBox || toSlot !in 0 until slotsPerBox) return false

        val sourceBox = boxes[fromBox]
        val targetBox = boxes[toBox]

        // Execute the swap
        val temp = sourceBox.get(fromSlot)
        sourceBox.set(fromSlot, targetBox.get(toSlot))
        targetBox.set(toSlot, temp)

        return true
    }
}