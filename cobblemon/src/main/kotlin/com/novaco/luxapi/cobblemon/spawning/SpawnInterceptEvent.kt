package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

/**
 * A simplified view of a Cobblemon [SpawnEvent] for a [PokemonEntity], as delivered by
 * [SpawnInterceptor.onPokemonSpawn].
 *
 * @property entity The Pokémon entity about to be spawned into the world.
 * @property world The dimension the spawn is happening in.
 * @property position The block position of the spawn attempt.
 * @property species The species name being spawned (e.g. "Bulbasaur").
 */
data class SpawnInterceptEvent(
    val entity: PokemonEntity,
    val world: ServerLevel,
    val position: BlockPos,
    val species: String,
    private val source: SpawnEvent<PokemonEntity>
) {
    /** Prevents this Pokémon from spawning. */
    fun cancel() {
        source.cancel()
    }
}
