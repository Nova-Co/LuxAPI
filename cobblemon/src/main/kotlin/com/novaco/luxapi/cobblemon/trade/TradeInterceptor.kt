package com.novaco.luxapi.cobblemon.trade

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.TradeEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription

/**
 * A hook into Cobblemon's native player-to-player trade system (`TradeManager`), as opposed to
 * [com.novaco.luxapi.cobblemon.gts.GlobalTradeManager]'s asynchronous GTS listings. Fires for
 * every direct trade attempt between two online players.
 */
object TradeInterceptor {

    /**
     * Registers a listener that runs for every player-to-player trade attempt, before it
     * completes. Call [TradeInterceptEvent.cancel] from within [listener] to prevent that
     * trade. Multiple listeners can be registered independently; any one of them cancelling a
     * trade cancels it for all.
     *
     * @param listener Runs once per trade attempt.
     */
    fun onTradeAttempt(listener: (TradeInterceptEvent) -> Unit): ObservableSubscription<TradeEvent.Pre> {
        return CobblemonEvents.TRADE_EVENT_PRE.subscribe { event ->
            listener(
                TradeInterceptEvent(
                    participant1Uuid = event.tradeParticipant1.uuid,
                    participant1Pokemon = event.tradeParticipant1Pokemon,
                    participant2Uuid = event.tradeParticipant2.uuid,
                    participant2Pokemon = event.tradeParticipant2Pokemon,
                    source = event
                )
            )
        }
    }

    /**
     * Registers a listener that runs after a player-to-player trade has already completed.
     *
     * @param listener Runs once per completed trade.
     */
    fun onTradeCompleted(listener: (TradeCompletedEvent) -> Unit): ObservableSubscription<TradeEvent.Post> {
        return CobblemonEvents.TRADE_EVENT_POST.subscribe { event ->
            listener(
                TradeCompletedEvent(
                    participant1Uuid = event.tradeParticipant1.uuid,
                    participant1Pokemon = event.tradeParticipant1Pokemon,
                    participant2Uuid = event.tradeParticipant2.uuid,
                    participant2Pokemon = event.tradeParticipant2Pokemon
                )
            )
        }
    }
}
