package com.novaco.luxapi.cobblemon.trade

import com.cobblemon.mod.common.api.events.pokemon.TradeEvent
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID

/**
 * A simplified view of a Cobblemon [TradeEvent.Pre], delivered by [TradeInterceptor.onTradeAttempt].
 * Fires before a player-to-player trade completes.
 *
 * @property participant1Uuid UUID of the first trading participant.
 * @property participant1Pokemon The Pokémon participant 1 is offering.
 * @property participant2Uuid UUID of the second trading participant.
 * @property participant2Pokemon The Pokémon participant 2 is offering.
 */
data class TradeInterceptEvent(
    val participant1Uuid: UUID,
    val participant1Pokemon: Pokemon,
    val participant2Uuid: UUID,
    val participant2Pokemon: Pokemon,
    private val source: TradeEvent.Pre
) {
    /** Prevents this trade from completing. */
    fun cancel() {
        source.cancel()
    }
}
