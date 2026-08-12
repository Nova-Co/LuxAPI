package com.novaco.luxapi.cobblemon.riding

import com.cobblemon.mod.common.api.events.pokemon.RidePokemonEvent
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer

/**
 * A simplified view of a Cobblemon [RidePokemonEvent.Pre], delivered by
 * [RidingInterceptor.onRideAttempt]. Fires before a player starts riding a Pokémon.
 *
 * @property player The player attempting to ride.
 * @property pokemon The Pokémon entity being ridden.
 */
data class RideAttemptEvent(
    val player: ServerPlayer,
    val pokemon: PokemonEntity,
    private val source: RidePokemonEvent.Pre
) {
    /** Prevents this ride attempt from succeeding. */
    fun cancel() {
        source.cancel()
    }
}
