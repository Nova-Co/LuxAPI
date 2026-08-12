package com.novaco.luxapi.cobblemon.spawning

import com.novaco.luxapi.commons.math.Cuboid
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import java.util.UUID

/**
 * A region within which spawn rates and/or allowed species are overridden.
 * Managed by [AreaSpawnManager].
 *
 * @property id A unique identifier for this area.
 * @property dimension The dimension this area applies to.
 * @property region The axis-aligned region, in that dimension's block coordinates.
 * @property weightMultiplier Multiplies the spawn weight of every spawn attempt inside
 * this area. `0.0` blocks all spawns here, `1.0` is a no-op, `>1.0` boosts spawn rate.
 * @property bannedSpecies Species names (case-insensitive) that may never spawn inside
 * this area, regardless of [weightMultiplier].
 */
data class SpawnArea(
    val id: UUID = UUID.randomUUID(),
    val dimension: ResourceKey<Level>,
    val region: Cuboid,
    val weightMultiplier: Float = 1.0F,
    val bannedSpecies: Set<String> = emptySet()
)
