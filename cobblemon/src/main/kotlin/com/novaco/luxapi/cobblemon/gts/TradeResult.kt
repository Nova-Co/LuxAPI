package com.novaco.luxapi.cobblemon.gts

import com.cobblemon.mod.common.pokemon.Pokemon

sealed class TradeResult {
    data class Success(val pokemon: Pokemon) : TradeResult()
    data class Failure(val reason: String) : TradeResult()

    fun ifSuccessful(action: (Pokemon) -> Unit): TradeResult {
        if (this is Success) action(this.pokemon)
        return this
    }

    fun ifFailed(action: (String) -> Unit): TradeResult {
        if (this is Failure) action(this.reason)
        return this
    }
}