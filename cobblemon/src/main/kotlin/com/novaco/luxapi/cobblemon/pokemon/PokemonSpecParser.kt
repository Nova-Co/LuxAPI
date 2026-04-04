package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * A utility registry for parsing and generating Pokemon entities from strings.
 * Acts as a safe wrapper around Cobblemon's native PokemonProperties system,
 * preventing server crashes from malformed strings.
 */
object PokemonSpecParser {

    /**
     * Safely parses a string specification into a new Pokemon object.
     * Example: "pikachu lvl=50 shiny=yes form=gmax"
     *
     * @param spec The specification string.
     * @return The generated Pokemon, or null if the string is invalid or the species doesn't exist.
     */
    fun parse(spec: String): Pokemon? {
        return try {
            val properties = PokemonProperties.parse(spec)
            properties.create()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parses a string specification into a Pokemon object, throwing an exception on failure.
     * Use this ONLY when you are absolutely sure the spec is hardcoded or validated.
     *
     * @param spec The specification string.
     * @return The generated Pokemon.
     * @throws IllegalArgumentException If the parsing fails.
     */
    fun parseOrThrow(spec: String): Pokemon {
        try {
            val properties = PokemonProperties.parse(spec)
            return properties.create()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse Pokemon spec: '$spec'. Reason: ${e.message}", e)
        }
    }

    /**
     * Parses a spec and directly applies it to modify an EXISTING Pokemon.
     * Perfect for quest rewards (e.g., making a player's existing Pokemon shiny).
     * Example spec: "shiny=yes level=100"
     *
     * @param pokemon The existing Pokemon to modify.
     * @param spec The specification string with modifications.
     * @return True if the modification was successful, false if the spec was invalid.
     */
    fun modify(pokemon: Pokemon, spec: String): Boolean {
        return try {
            val properties = PokemonProperties.parse(spec)
            properties.apply(pokemon)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a given specification string is syntactically valid.
     * * @param spec The specification string to test.
     * @return True if valid, false otherwise.
     */
    fun isValid(spec: String): Boolean {
        return try {
            PokemonProperties.parse(spec)
            true
        } catch (e: Exception) {
            false
        }
    }
}