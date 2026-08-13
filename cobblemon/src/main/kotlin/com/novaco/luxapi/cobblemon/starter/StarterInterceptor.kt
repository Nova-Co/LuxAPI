package com.novaco.luxapi.cobblemon.starter

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.starter.StarterChosenEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription

/**
 * A hook into Cobblemon's own starter-selection flow, fired once a player's choice has
 * been resolved to a concrete [com.cobblemon.mod.common.pokemon.Pokemon] but before it's
 * actually granted.
 */
object StarterInterceptor {

    /**
     * Registers a listener that runs whenever a player finalizes a starter choice. Call
     * [StarterChosenInterceptEvent.cancel] to block the grant entirely, or
     * [StarterChosenInterceptEvent.replacePokemon] to give a different Pokémon instead.
     *
     * @param listener Runs once per starter choice.
     * @return The subscription handle. Cobblemon's event bus has no automatic listener
     * lifecycle — call `.unsubscribe()` on it if [listener] shouldn't outlive its caller.
     */
    fun onStarterChosen(listener: (StarterChosenInterceptEvent) -> Unit): ObservableSubscription<StarterChosenEvent> {
        return CobblemonEvents.STARTER_CHOSEN.subscribe { event ->
            listener(
                StarterChosenInterceptEvent(
                    player = event.player,
                    properties = event.properties,
                    pokemon = event.pokemon,
                    source = event
                )
            )
        }
    }
}
