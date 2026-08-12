package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.events.CobblemonEvents

/**
 * A hook into Cobblemon's natural Pokémon spawning, as opposed to [com.novaco.luxapi.cobblemon.manager.PokemonSpawner]'s
 * direct/manual spawning helpers. Fires for every wild Pokémon spawn attempt Cobblemon's own
 * spawner performs, before the entity is added to the world.
 */
object SpawnInterceptor {

    /**
     * Registers a listener that runs for every natural Pokémon spawn attempt. Call
     * [SpawnInterceptEvent.cancel] from within [listener] to prevent that spawn.
     * Multiple listeners can be registered independently; any one of them cancelling
     * a spawn cancels it for all.
     *
     * @param listener Runs once per spawn attempt.
     */
    fun onPokemonSpawn(listener: (SpawnInterceptEvent) -> Unit) {
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { event ->
            listener(
                SpawnInterceptEvent(
                    entity = event.entity,
                    world = event.spawnablePosition.world,
                    position = event.spawnablePosition.position,
                    species = event.entity.pokemon.species.name,
                    source = event
                )
            )
        }
    }
}
