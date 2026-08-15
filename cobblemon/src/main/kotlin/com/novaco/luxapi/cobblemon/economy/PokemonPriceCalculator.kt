package com.novaco.luxapi.cobblemon.economy

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.pokemon.getIVPercentage
import java.util.UUID

/**
 * An example [PokemonAppraiser] implementation — not registered by default. A consumer opts in
 * explicitly via `LuxPokemonEconomy.registerAppraiser(PokemonPriceCalculator)` if this formula
 * (or a copy adapted to their own server) fits their needs; [LuxPokemonEconomy] itself ships with
 * no active appraiser out of the box.
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
}