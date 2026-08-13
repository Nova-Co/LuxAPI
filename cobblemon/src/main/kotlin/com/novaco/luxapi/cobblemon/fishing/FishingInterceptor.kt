package com.novaco.luxapi.cobblemon.fishing

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BobberSpawnPokemonEvent
import com.cobblemon.mod.common.api.events.fishing.PokerodCastEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription

/**
 * Hooks into Cobblemon's fishing-rod flow. Each listener here mirrors one real
 * Cobblemon event honestly rather than forcing all three into one cancel-shaped API —
 * [onCast] and [onBobberSpawnAttempt] are cancelable (fired before the outcome is
 * decided), [onPokemonHooked] is not (fired after the Pokémon already exists; see
 * [FishingCatchEvent]'s KDoc).
 *
 * **Known gap:** unlike [com.novaco.luxapi.cobblemon.drop.DropInterceptor] and
 * [com.novaco.luxapi.cobblemon.starter.StarterInterceptor], these three have no unit
 * tests — Cobblemon's `PokerodCastEvent.Pre`/`Post` and
 * `BobberSpawnPokemonEvent.Pre`/`Post` build a MoLang context map in their constructor
 * that calls a live-server-dependent utility (`server()!!`), so they can't be
 * constructed at all outside a running Minecraft server. This is a Cobblemon-side
 * constraint, not something this wrapper can work around.
 */
object FishingInterceptor {

    /**
     * Registers a listener that runs whenever a player casts a Poké Rod. Call
     * [FishingCastInterceptEvent.cancel] to prevent the cast.
     *
     * @return The subscription handle. Cobblemon's event bus has no automatic listener
     * lifecycle — call `.unsubscribe()` on it if [listener] shouldn't outlive its caller.
     */
    fun onCast(listener: (FishingCastInterceptEvent) -> Unit): ObservableSubscription<PokerodCastEvent.Pre> {
        return CobblemonEvents.POKEROD_CAST_PRE.subscribe { event ->
            listener(FishingCastInterceptEvent(rod = event.rod, bait = event.bait, bobber = event.bobber, source = event))
        }
    }

    /**
     * Registers a listener that runs before a bobber spawns a Pokémon in. Call
     * [FishingSpawnInterceptEvent.cancel] to prevent that spawn.
     *
     * @return The subscription handle (see [onCast]'s KDoc).
     */
    fun onBobberSpawnAttempt(listener: (FishingSpawnInterceptEvent) -> Unit): ObservableSubscription<BobberSpawnPokemonEvent.Pre> {
        return CobblemonEvents.BOBBER_SPAWN_POKEMON_PRE.subscribe { event ->
            listener(FishingSpawnInterceptEvent(bobber = event.bobber, rod = event.rod, spawnAction = event.spawnAction, source = event))
        }
    }

    /**
     * Registers a listener that runs after a bobber has actually produced a caught
     * Pokémon. Not cancelable — see [FishingCatchEvent]'s KDoc.
     *
     * @return The subscription handle (see [onCast]'s KDoc).
     */
    fun onPokemonHooked(listener: (FishingCatchEvent) -> Unit): ObservableSubscription<BobberSpawnPokemonEvent.Post> {
        return CobblemonEvents.BOBBER_SPAWN_POKEMON_POST.subscribe { event ->
            listener(FishingCatchEvent(bobber = event.bobber, bait = event.bait, pokemon = event.pokemon))
        }
    }
}
