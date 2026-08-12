package com.novaco.luxapi.cobblemon.riding

import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer

/**
 * Read-only queries about a Pokémon's *current* riding state. For hooking into ride
 * attempts/completions/stamina consumption as they happen, see [RidingInterceptor] instead.
 */
object RidingQuery {

    /** Whether [entity] currently has a rider mounted on it. */
    fun isBeingRidden(entity: PokemonEntity): Boolean {
        return entity.controllingPassenger != null
    }

    /** The player currently riding [entity], or `null` if nobody is. */
    fun getRider(entity: PokemonEntity): ServerPlayer? {
        return entity.controllingPassenger as? ServerPlayer
    }

    /** The active riding medium ([RidingStyle.LAND]/`.LIQUID`/`.AIR`), or `null` if not being ridden. */
    fun getRidingStyle(entity: PokemonEntity): RidingStyle? {
        return entity.ridingController?.context?.style
    }

    /** Current ride stamina (`0.0`-`1.0`), or `null` if [entity] isn't actively being ridden. */
    fun getStamina(entity: PokemonEntity): Float? {
        return entity.ridingController?.context?.state?.stamina?.get()
    }

    /**
     * The raw (unscaled, `0.0`-`1.0`) value of [stat] for [entity] under [style], derived from
     * the Pokémon's own riding stat boosts. `0.0` if [entity] has no behaviour configured for
     * [style].
     */
    fun getRideStat(entity: PokemonEntity, stat: RidingStat, style: RidingStyle): Double {
        return entity.getRawRideStat(stat, style)
    }

    /**
     * Forcibly dismounts every rider from [entity]. Returns `false` if nobody was riding it.
     */
    fun dismount(entity: PokemonEntity): Boolean {
        val hadRider = entity.passengers.isNotEmpty()
        entity.ejectPassengers()
        return hadRider
    }
}
