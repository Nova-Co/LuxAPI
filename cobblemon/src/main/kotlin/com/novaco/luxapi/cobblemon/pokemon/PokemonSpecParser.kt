package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory

/**
 * A utility registry for parsing and generating Pokemon entities from strings.
 * Upgraded to safely interface with modern Cobblemon genetic matrices and aspect layers.
 */
object PokemonSpecParser {

    private val logger = LoggerFactory.getLogger(PokemonSpecParser::class.java)

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
            logger.warn("Invalid Pokemon spec syntax or argument in '{}': {}", spec, e.message)
            null
        } catch (e: IllegalStateException) {
            logger.warn("Failed to instantiate Pokemon from spec '{}' (Missing species or registry error): {}", spec, e.message)
            null
        } catch (e: NullPointerException) {
            logger.warn("Null reference encountered while resolving species/properties for spec '{}': {}", spec, e.message)
            null
        } catch (e: UnsupportedOperationException) {
            logger.warn("Unsupported property configuration for spec '{}': {}", spec, e.message)
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
            logger.warn("Spec parse error during modification for Pokemon {}: {}", pokemon.uuid, e.message)
            false
        } catch (e: IllegalStateException) {
            logger.warn("Invalid state while applying spec to Pokemon {}: {}", pokemon.uuid, e.message)
            false
        } catch (e: UnsupportedOperationException) {
            logger.warn("Property mismatch while applying spec to Pokemon {}: {}", pokemon.uuid, e.message)
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
            logger.warn("Invalid aspect label key '{}' for Pokemon {}: {}", aspectKey, pokemon.uuid, e.message)
        } catch (e: UnsupportedOperationException) {
            logger.warn("Mutation blocked on forcedAspects set for Pokemon {}: {}", pokemon.uuid, e.message)
        } catch (e: IllegalStateException) {
            logger.warn("Cannot append aspect to Pokemon {} in current state: {}", pokemon.uuid, e.message)
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