package com.novaco.luxapi.cobblemon.economy

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.pokemon.getIVPercentage
import java.util.UUID

/**
 * A highly advanced utility engine for calculating the dynamic market value of a Pokémon.
 * Deeply integrates with Pokedex historical indexes and pipes data directly into the LuxEC module.
 */
object PokemonPriceCalculator {

    /**
     * Calculates the estimated market value of a given Pokémon with dynamic weighting structures.
     *
     * @param playerUuid The UUID of the player participating in the evaluation context (for Pokedex checks).
     * @param pokemon The target [Pokemon] to be evaluated.
     * @param basePrice The starting configuration price for a standard Pokémon.
     * @return The final evaluated monetary value.
     */
    fun calculateValue(playerUuid: UUID, pokemon: Pokemon, basePrice: Double = 1000.0): Double {
        var finalPrice = basePrice
        val labels = pokemon.form.labels

        // 1. Rarity Label Weighting Evaluation
        when {
            labels.contains("legendary") -> finalPrice *= 10.0
            labels.contains("mythical") -> finalPrice *= 15.0
            labels.contains("ultra_beast") -> finalPrice *= 8.0
        }

        // 2. Visual / Skin Trait Weighting
        if (pokemon.shiny) {
            finalPrice *= 5.0
        }

        // 3. Ability Template Weighting
        val hasHiddenAbility = pokemon.form.abilities.any { potentialAbility ->
            potentialAbility is com.cobblemon.mod.common.pokemon.abilities.HiddenAbility &&
                    potentialAbility.template.name == pokemon.ability.template.name
        }
        if (hasHiddenAbility) {
            finalPrice *= 2.0
        }

        // 4. Genetic Perfection Weighting Matrix
        val ivPercentage = pokemon.getIVPercentage()
        val ivMultiplier = 1.0 + (ivPercentage / 100.0)
        finalPrice *= ivMultiplier

        if (ivPercentage >= 100.0) {
            finalPrice *= 3.0
        }

        // 5. Dynamic Check Against Pokedex Completion Matrix
        try {
            val pokedexClass = Class.forName("com.novaco.luxapi.cobblemon.pokedex.PokedexManager")
            val isCaughtMethod = pokedexClass.getMethod("hasCaught", UUID::class.java, String::class.java)
            val alreadyCaught = isCaughtMethod.invoke(null, playerUuid, pokemon.species.name) as Boolean

            // Apply scarcity bonus weight if this species is a brand-new discovery for the player
            if (!alreadyCaught) {
                finalPrice *= 1.5
                println("[LuxAPI | Economy] Applied Scarcity Discovery Weight for ${pokemon.species.name}")
            }
        } catch (e: Exception) {
            // Soft fallback to prevent system freeze if Pokedex module is detached
        }

        return finalPrice
    }

    /**
     * Directly process an asset conversion value and flush states cleanly to the LuxEC Module pipeline.
     */
    fun processTransactionToLuxEC(playerUuid: UUID, pokemon: Pokemon, basePrice: Double = 1000.0) {
        val totalWorth = calculateValue(playerUuid, pokemon, basePrice)
        try {
            val luxEcClass = Class.forName("com.novaco.luxapi.economy.LuxEC")
            val depositMethod = luxEcClass.getMethod("depositPlayerBalance", UUID::class.java, Double::class.java)
            depositMethod.invoke(null, playerUuid, totalWorth)
            println("[LuxAPI] Dispatched transaction worth $totalWorth directly over to LuxEC pipeline.")
        } catch (cnfe: ClassNotFoundException) {
            println("[LuxAPI | Economy Alert] LuxEC module is absent. Monetary value calculation skipped from banking.")
        } catch (t: Throwable) {
            println("[LuxAPI] Failed to complete dynamic economy dispatch loop: ${t.message}")
        }
    }
}