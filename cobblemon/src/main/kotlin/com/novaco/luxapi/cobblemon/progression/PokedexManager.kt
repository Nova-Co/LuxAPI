package com.novaco.luxapi.cobblemon.progression

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.util.pokedex
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

/**
 * A centralized manager for handling Pokédex-related progression.
 * This utility allows developers to easily check a player's Pokédex completion,
 * verify if specific Pokémon have been caught or seen, and build reward systems.
 */
object PokedexManager {

    /**
     * Checks if the player has successfully caught a specific Pokémon species.
     *
     * @param player The target player.
     * @param speciesName The name of the Pokémon species (e.g., "pikachu").
     * @return True if the player has caught the species, false otherwise.
     */
    fun hasCaught(player: LuxPlayer, speciesName: String): Boolean {
        val serverPlayer = player.parent as ServerPlayer
        val species = PokemonSpecies.getByName(speciesName.lowercase()) ?: return false

        // Fetch the specific record for this Pokémon species
        val record = serverPlayer.pokedex().speciesRecords[species.resourceIdentifier] ?: return false

        // 🌟 Fix: Use the internal knowledge enum to verify if the status is CAUGHT
        return record.getKnowledge().name.equals("CAUGHT", ignoreCase = true)
    }

    /**
     * Checks if the player has encountered (seen) a specific Pokémon species.
     *
     * @param player The target player.
     * @param speciesName The name of the Pokémon species.
     * @return True if the player has seen the species, false otherwise.
     */
    fun hasSeen(player: LuxPlayer, speciesName: String): Boolean {
        val serverPlayer = player.parent as ServerPlayer
        val species = PokemonSpecies.getByName(speciesName.lowercase()) ?: return false

        // Fetch the specific record for this Pokémon species
        val record = serverPlayer.pokedex().speciesRecords[species.resourceIdentifier] ?: return false

        // 🌟 Fix: If the record exists and knowledge isn't NONE, it means it has been seen/encountered.
        return !record.getKnowledge().name.equals("NONE", ignoreCase = true)
    }

    /**
     * Retrieves the total number of unique Pokémon species the player has caught.
     * Useful for milestone rewards (e.g., "Catch 100 unique Pokémon").
     *
     * @param player The target player.
     * @return The total count of registered caught species.
     */
    fun getCaughtCount(player: LuxPlayer): Int {
        val serverPlayer = player.parent as ServerPlayer

        return serverPlayer.pokedex().speciesRecords.values.count {
            it.getKnowledge().name.equals("CAUGHT", ignoreCase = true)
        }
    }

    /**
     * Calculates the player's overall Pokédex completion percentage.
     *
     * @param player The target player.
     * @param totalAvailableSpecies The maximum number of species available on the server (default is 1025).
     * @return A double representing the completion percentage (0.0 to 100.0).
     */
    fun getCompletionPercentage(player: LuxPlayer, totalAvailableSpecies: Int = 1025): Double {
        val caughtCount = getCaughtCount(player)
        if (caughtCount == 0 || totalAvailableSpecies <= 0) return 0.0

        return (caughtCount.toDouble() / totalAvailableSpecies.toDouble()) * 100.0
    }
}