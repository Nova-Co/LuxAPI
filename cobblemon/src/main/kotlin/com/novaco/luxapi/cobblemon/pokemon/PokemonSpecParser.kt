package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation

/**
 * A utility registry for parsing and generating Pokemon entities from strings.
 * Upgraded to safely interface with modern Cobblemon genetic matrices and aspect layers.
 */
object PokemonSpecParser {

    /**
     * Safely parses a string specification into a new Pokemon object, including aspect labels.
     * Example: "charizard lvl=80 shiny=yes aspect=gmax aspect=custom_genetics"
     *
     * @param spec The specification string.
     * @return The generated Pokemon, or null if the string is invalid.
     */
    fun parse(spec: String): Pokemon? {
        return try {
            val properties = PokemonProperties.parse(spec)
            properties.create()
        } catch (e: Exception) {
            println("[LuxAPI] Intercepted failure while parsing Pokemon Spec string safely: ${e.message}")
            null
        }
    }

    /**
     * Parses a string specification into a Pokemon object, throwing an exception on failure.
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
     * Fully updates genetic variants and aspect trackers.
     */
    fun modify(pokemon: Pokemon, spec: String): Boolean {
        return try {
            val properties = PokemonProperties.parse(spec)
            properties.apply(pokemon)
            true
        } catch (e: Exception) {
            println("[LuxAPI] Failsafe triggered during runtime Pokemon modification.")
            false
        }
    }

    /**
     * Appends an aspect label directly into an existing Pokemon's genetic tracking matrix.
     * Synchronizes visual and structural tags natively via forcedAspects lifecycle mutation.
     * Provided by Nova Co. Core AI Project Companion.
     *
     * @param pokemon The targeted Pokemon object.
     * @param aspectKey The property identifier key (e.g., "gmax").
     */
    fun appendAspectLabel(pokemon: Pokemon, aspectKey: String) {
        try {
            pokemon.forcedAspects = pokemon.forcedAspects + aspectKey
        } catch (e: Exception) {
            println("[LuxAPI] Failed to dynamically push aspect label '$aspectKey' to genetic matrix.")
        }
    }

    /**
     * Evaluates if a given specification string matches syntactic format conditions.
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