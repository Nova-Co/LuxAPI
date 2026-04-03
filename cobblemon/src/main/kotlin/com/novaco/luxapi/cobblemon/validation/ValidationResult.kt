package com.novaco.luxapi.cobblemon.validation

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Represents the result of a party validation check.
 */
data class ValidationResult(val isValid: Boolean, val reason: String? = null)

/**
 * The base interface for all competitive party rules.
 * Developers can create custom rules by implementing this interface.
 */
interface PartyRule {
    val ruleName: String
    fun validate(party: List<Pokemon>): ValidationResult
}