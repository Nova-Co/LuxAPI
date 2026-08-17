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
        } catch (e: IllegalArgumentException) {
            println("[LuxAPI] Invalid Pokemon spec syntax or argument in '$spec': ${e.message}")
            null
        } catch (e: IllegalStateException) {
            println("[LuxAPI] Failed to instantiate Pokemon from spec '$spec' (Missing species or registry error): ${e.message}")
            null
        } catch (e: NullPointerException) {
            println("[LuxAPI] Null reference encountered while resolving species/properties for spec '$spec': ${e.message}")
            null
        } catch (e: UnsupportedOperationException) {
            println("[LuxAPI] Unsupported property configuration for spec '$spec': ${e.message}")
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
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid Pokemon spec syntax: '$spec'. Reason: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw IllegalArgumentException("Pokemon creation rejected for spec: '$spec'. Reason: ${e.message}", e)
        } catch (e: UnsupportedOperationException) {
            throw IllegalArgumentException("Unsupported property applied in spec: '$spec'. Reason: ${e.message}", e)
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
        } catch (e: IllegalArgumentException) {
            println("[LuxAPI] Spec parse error during modification for Pokemon ${pokemon.uuid}: ${e.message}")
            false
        } catch (e: IllegalStateException) {
            println("[LuxAPI] Invalid state while applying spec to Pokemon ${pokemon.uuid}: ${e.message}")
            false
        } catch (e: UnsupportedOperationException) {
            println("[LuxAPI] Property mismatch while applying spec to Pokemon ${pokemon.uuid}: ${e.message}")
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
        } catch (e: IllegalArgumentException) {
            println("[LuxAPI] Invalid aspect label key '$aspectKey' for Pokemon ${pokemon.uuid}: ${e.message}")
        } catch (e: UnsupportedOperationException) {
            println("[LuxAPI] Mutation blocked on forcedAspects set for Pokemon ${pokemon.uuid}: ${e.message}")
        } catch (e: IllegalStateException) {
            println("[LuxAPI] Cannot append aspect to Pokemon ${pokemon.uuid} in current state: ${e.message}")
        }
    }

    /**
     * Evaluates if a given specification string matches syntactic format conditions.
     */
    fun isValid(spec: String): Boolean {
        return try {
            PokemonProperties.parse(spec)
            true
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: IllegalStateException) {
            false
        }
    }
}