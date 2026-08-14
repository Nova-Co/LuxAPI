package com.novaco.luxapi.cobblemon.riding

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.RidePokemonEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription

/**
 * A hook into Cobblemon's native riding lifecycle — ride attempts, completed mounts, and
 * per-tick stamina consumption. For read-only queries of a Pokémon's *current* riding state
 * (is it being ridden right now, by whom, at what stamina), see [RidingQuery] instead.
 */
object RidingInterceptor {

    /**
     * Registers a listener that runs for every ride attempt, before it succeeds. Call
     * [RideAttemptEvent.cancel] from within [listener] to prevent that ride. Multiple listeners
     * can be registered independently; any one of them cancelling a ride cancels it for all.
     *
     * @param listener Runs once per ride attempt.
     */
    fun onRideAttempt(listener: (RideAttemptEvent) -> Unit): ObservableSubscription<RidePokemonEvent.Pre> {
        return CobblemonEvents.RIDE_EVENT_PRE.subscribe { event ->
            listener(RideAttemptEvent(event.player, event.pokemon, event))
        }
    }

    /**
     * Registers a listener that runs after a player has started riding a Pokémon.
     *
     * @param listener Runs once per completed ride start.
     */
    fun onRideCompleted(listener: (RideCompletedEvent) -> Unit): ObservableSubscription<RidePokemonEvent.Post> {
        return CobblemonEvents.RIDE_EVENT_POST.subscribe { event ->
            listener(RideCompletedEvent(event.player, event.pokemon))
        }
    }

    /**
     * Registers a listener that runs every tick stamina is being consumed while a Pokémon is
     * ridden. Modify [RideStaminaEvent.stamina] (or call [RideStaminaEvent.setInfiniteStamina])
     * from within [listener] to change how much is used.
     *
     * @param listener Runs once per stamina-consuming tick.
     */
    fun onStaminaApply(listener: (RideStaminaEvent) -> Unit): ObservableSubscription<RidePokemonEvent.ApplyStamina> {
        return CobblemonEvents.RIDE_EVENT_APPLY_STAMINA.subscribe { event ->
            listener(RideStaminaEvent(event.player, event.pokemon, event))
        }
    }
}
