package com.novaco.luxapi.cobblemon.riding

import com.cobblemon.mod.common.api.events.pokemon.RidePokemonEvent
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer

/**
 * A simplified view of a Cobblemon `RidePokemonEvent.ApplyStamina`, delivered by
 * [RidingInterceptor.onStaminaApply]. Fires every tick stamina is being consumed while riding —
 * [stamina] can be read or overwritten to change how much is used this tick.
 *
 * @property player The player currently riding.
 * @property pokemon The Pokémon entity being ridden.
 */
class RideStaminaEvent(
    val player: ServerPlayer,
    val pokemon: PokemonEntity,
    private val source: RidePokemonEvent.ApplyStamina
) {
    /** The stamina amount about to be applied. `-1F` means infinite (see [setInfiniteStamina]). */
    var stamina: Float
        get() = source.rideStamina
        set(value) {
            source.rideStamina = value
        }

    /** Makes this tick's stamina consumption infinite (i.e. free). */
    fun setInfiniteStamina() {
        source.setInfiniteStamina()
    }
}
