package com.novaco.luxapi.cobblemon.riding

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer

/**
 * A simplified view of a Cobblemon `RidePokemonEvent.Post`, delivered by
 * [RidingInterceptor.onRideCompleted]. Fires after a player has already started riding — there
 * is nothing left to cancel.
 *
 * @property player The player now riding.
 * @property pokemon The Pokémon entity being ridden.
 */
data class RideCompletedEvent(
    val player: ServerPlayer,
    val pokemon: PokemonEntity
)
