package com.novaco.luxapi.cobblemon.validation

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Enforces that no two Pokémon in the party share the same Pokédex number/species.
 */
class SpeciesClause : PartyRule {
    override val ruleName = "Species Clause"

    override fun validate(party: List<Pokemon>): ValidationResult {
        val seenSpecies = mutableSetOf<String>()

        for (pokemon in party) {
            val speciesName = pokemon.species.name.lowercase()
            if (!seenSpecies.add(speciesName)) {
                return ValidationResult(false, "Rule Violation: Duplicate species found (${pokemon.species.name}).")
            }
        }
        return ValidationResult(true)
    }
}

/**
 * Enforces that no two Pokémon in the party are holding the exact same item.
 */
class ItemClause : PartyRule {
    override val ruleName = "Item Clause"

    override fun validate(party: List<Pokemon>): ValidationResult {
        val seenItems = mutableSetOf<String>()

        for (pokemon in party) {
            val heldItem = pokemon.heldItem()
            if (!heldItem.isEmpty) {
                val itemId = heldItem.item.descriptionId
                if (!seenItems.add(itemId)) {
                    return ValidationResult(false, "Rule Violation: Duplicate held item found (${heldItem.hoverName.string}).")
                }
            }
        }
        return ValidationResult(true)
    }
}

/**
 * Bans any Pokémon labeled as Legendary, Mythical, or Ultra Beast.
 */
class NoLegendariesRule : PartyRule {
    override val ruleName = "No Legendaries"

    override fun validate(party: List<Pokemon>): ValidationResult {
        for (pokemon in party) {
            if (pokemon.isLegendary() || pokemon.isMythical() || pokemon.isUltraBeast()) {
                return ValidationResult(false, "Rule Violation: Legendary/Mythical Pokémon are banned (${pokemon.species.name}).")
            }
        }
        return ValidationResult(true)
    }
}