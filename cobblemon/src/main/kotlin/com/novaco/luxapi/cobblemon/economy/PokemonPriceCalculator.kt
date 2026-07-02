package com.novaco.luxapi.cobblemon.economy

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.pokemon.getIVPercentage
import java.util.UUID

/**
 * Default implementation of the pricing rules used by Nova Co. network.
 * Act as a built-in appraiser inside the LuxPokemonEconomy ecosystem.
 * Provided by Nova Co. Core AI Project Companion.
 */
object PokemonPriceCalculator : PokemonAppraiser {

    /**
     * Natively implements the appraise function from PokemonAppraiser interface.
     */
    override fun appraise(playerUuid: UUID, pokemon: Pokemon, currentPrice: Double): Double {
        var finalPrice = currentPrice
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
     * Dispatches the evaluated asset worth cleanly using the centralized economy pipelines.
     */
    fun processTransactionToLuxEC(playerUuid: UUID, pokemon: Pokemon, basePrice: Double = 1000.0) {
        // Pipes valuation directly through the customizable economy framework instead of hardcoded loop
        val totalWorth = LuxPokemonEconomy.evaluatePokemonWorth(playerUuid, pokemon, basePrice)
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