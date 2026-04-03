package com.novaco.luxapi.cobblemon.economy

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.pokemon.getIVPercentage

/**
 * A utility engine for calculating the monetary or economic value of a Pokémon.
 * Inspired by traditional pricing algorithms, this evaluates a Pokémon based on
 * its rarity, IVs, shiny status, and hidden abilities.
 * * Highly useful for Global Trade Systems (GTS), Server Shops, or Black Market mechanics.
 *
 */
object PokemonPriceCalculator {

    /**
     * Calculates the estimated market value of a given Pokémon.
     * Developers can adjust the base prices and multipliers to fit their server's economy.
     *
     * @param pokemon The [Pokemon] to be evaluated.
     * @param basePrice The starting price for a standard, non-rare Pokémon.
     * @return The final calculated price as a Double.
     */
    fun calculateValue(pokemon: Pokemon, basePrice: Double = 1000.0): Double {
        var finalPrice = basePrice
        val labels = pokemon.form.labels

        when {
            labels.contains("legendary") -> finalPrice *= 10.0
            labels.contains("mythical") -> finalPrice *= 15.0
            labels.contains("ultra_beast") -> finalPrice *= 8.0
        }

        if (pokemon.shiny) {
            finalPrice *= 5.0
        }

        val hasHiddenAbility = pokemon.form.abilities.any { potentialAbility ->
            potentialAbility is com.cobblemon.mod.common.pokemon.abilities.HiddenAbility &&
                    potentialAbility.template.name == pokemon.ability.template.name
        }

        if (hasHiddenAbility) {
            finalPrice *= 2.0
        }

        val ivPercentage = pokemon.getIVPercentage()
        val ivMultiplier = 1.0 + (ivPercentage / 100.0)
        finalPrice *= ivMultiplier

        if (ivPercentage >= 100.0) {
            finalPrice *= 3.0
        }

        return finalPrice
    }
}