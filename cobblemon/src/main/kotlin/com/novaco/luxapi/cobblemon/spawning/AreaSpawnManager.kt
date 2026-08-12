package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import java.util.UUID

/**
 * Registry of [SpawnArea]s. See [LuxAreaSpawningInfluence] for how these areas are
 * actually enforced against Cobblemon's spawner.
 */
object AreaSpawnManager {

    private val areas = mutableMapOf<UUID, SpawnArea>()

    init {
        PlayerSpawnerFactory.influenceBuilders.add { LuxAreaSpawningInfluence }
    }

    /**
     * Registers a new spawn area.
     *
     * @param area The area configuration.
     * @return The area's unique ID, which can be used to unregister it later.
     */
    fun registerArea(area: SpawnArea): UUID {
        areas[area.id] = area
        return area.id
    }

    /**
     * Removes a previously registered spawn area. Unknown ids are a silent no-op.
     *
     * @param id The ID returned by [registerArea].
     */
    fun unregisterArea(id: UUID) {
        areas.remove(id)
    }

    /**
     * Retrieves all currently registered areas for the given dimension.
     *
     * @param dimension The dimension to filter by.
     * @return The matching areas, in no particular order.
     */
    fun getAreas(dimension: ResourceKey<Level>): List<SpawnArea> {
        return areas.values.filter { it.dimension == dimension }
    }
}

/**
 * Live-queries [AreaSpawnManager]'s registry on every call — deliberately stateless,
 * so registering or unregistering a [SpawnArea] takes effect immediately for every
 * player, including ones whose [com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawner]
 * was already created. Not part of the public API surface; use [AreaSpawnManager] instead.
 */
internal object LuxAreaSpawningInfluence : SpawningInfluence {

    override fun affectSpawnable(detail: SpawnDetail, spawnablePosition: SpawnablePosition): Boolean {
        if (detail !is PokemonSpawnDetail) return true
        val species = detail.pokemon.species?.lowercase() ?: return true
        val areasHere = areasAt(spawnablePosition)
        return areasHere.none { species in it.bannedSpecies.map(String::lowercase) }
    }

    override fun affectWeight(detail: SpawnDetail, spawnablePosition: SpawnablePosition, weight: Float): Float {
        var result = weight
        for (area in areasAt(spawnablePosition)) {
            result *= area.weightMultiplier
        }
        return result
    }

    private fun areasAt(spawnablePosition: SpawnablePosition): List<SpawnArea> {
        val pos = spawnablePosition.position
        return AreaSpawnManager.getAreas(spawnablePosition.world.dimension())
            .filter { it.region.contains(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) }
    }
}
