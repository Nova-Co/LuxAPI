package com.novaco.luxapi.cobblemon.spawning

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import java.util.UUID

/**
 * Registry of [SpawnArea]s. See [LuxAreaSpawningInfluence] for how these areas are
 * actually enforced against Cobblemon's spawner.
 */
object AreaSpawnManager {

    private val areas = mutableMapOf<UUID, SpawnArea>()

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
