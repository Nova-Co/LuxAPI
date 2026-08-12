package com.novaco.luxapi.cobblemon.trade

import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.UUID

/**
 * A simplified view of a Cobblemon `TradeEvent.Post`, delivered by
 * [TradeInterceptor.onTradeCompleted]. Fires after a player-to-player trade has already
 * completed — there is nothing left to cancel.
 *
 * @property participant1Uuid UUID of the first trading participant.
 * @property participant1Pokemon The Pokémon participant 1 traded away.
 * @property participant2Uuid UUID of the second trading participant.
 * @property participant2Pokemon The Pokémon participant 2 traded away.
 */
data class TradeCompletedEvent(
    val participant1Uuid: UUID,
    val participant1Pokemon: Pokemon,
    val participant2Uuid: UUID,
    val participant2Pokemon: Pokemon
)
