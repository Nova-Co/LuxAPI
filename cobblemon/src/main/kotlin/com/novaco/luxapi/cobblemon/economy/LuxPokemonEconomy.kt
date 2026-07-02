package com.novaco.luxapi.cobblemon.economy

import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID

/**
 * Extensible Valuation Engine for Cobblemon Assets.
 * Allows external developers to register custom appraisal formulas and tap into transaction pipelines.
 */
object LuxPokemonEconomy {

    private val appraisers = mutableListOf<PokemonAppraiser>()

    /**
     * Registers a custom appraisal formula into the global valuation network.
     */
    fun registerAppraiser(appraiser: PokemonAppraiser) {
        appraisers.add(appraiser)
    }

    /**
     * Evaluates a Pokemon by passing it through all registered developer appraisers.
     */
    fun evaluatePokemonWorth(playerUuid: UUID, pokemon: Pokemon, basePrice: Double): Double {
        var currentWorth = basePrice
        for (appraiser in appraisers) {
            currentWorth = appraiser.appraise(playerUuid, pokemon, currentWorth)
        }
        return currentWorth
    }
}

/**
 * Functional Interface for custom developer price rules.
 */
fun interface PokemonAppraiser {
    fun appraise(playerUuid: UUID, pokemon: Pokemon, currentPrice: Double): Double
}