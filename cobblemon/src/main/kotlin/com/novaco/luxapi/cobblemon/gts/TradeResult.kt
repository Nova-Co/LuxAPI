package com.novaco.luxapi.cobblemon.gts

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Represents the outcome of a trade transaction in the Global Trade System.
 * This is a sealed class, which allows for exhaustive handling of both success and failure cases.
 */
sealed class TradeResult {
    /**
     * Indicates that the trade was completed successfully.
     * @property pokemon The Pokémon that was acquired in the trade.
     */
    data class Success(val pokemon: Pokemon) : TradeResult()

    /**
     * Indicates that the trade failed.
     * @property reason A user-friendly string explaining the reason for the failure.
     */
    data class Failure(val reason: String) : TradeResult()

    /**
     * Executes a given action if the result is [Success].
     * This enables a fluent, chainable style of handling results.
     *
     * @param action The code block to execute with the successfully traded Pokémon.
     * @return The original [TradeResult] instance for further chaining.
     */
    fun ifSuccessful(action: (Pokemon) -> Unit): TradeResult {
        if (this is Success) action(this.pokemon)
        return this
    }

    /**
     * Executes a given action if the result is [Failure].
     * This enables a fluent, chainable style of handling results.
     *
     * @param action The code block to execute with the failure reason.
     * @return The original [TradeResult] instance for further chaining.
     */
    fun ifFailed(action: (String) -> Unit): TradeResult {
        if (this is Failure) action(this.reason)
        return this
    }
}