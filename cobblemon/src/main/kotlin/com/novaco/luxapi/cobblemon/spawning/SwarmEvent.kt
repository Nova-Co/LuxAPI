package com.novaco.luxapi.cobblemon.spawning

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import java.util.UUID

/**
 * Represents a temporary mass-outbreak event of a specific Pokémon species.
 */
data class SwarmEvent(
    val id: UUID = UUID.randomUUID(),
    val speciesName: String,
    val dimension: ResourceKey<Level>,
    val centerPos: BlockPos,
    val radius: Int,
    val minLevel: Int = 5,
    val maxLevel: Int = 15,
    val maxActiveEntities: Int = 20,
    val expireAt: Long
) {
    val activeEntities = mutableSetOf<UUID>()

    fun isExpired(): Boolean = System.currentTimeMillis() > expireAt
}