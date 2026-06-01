package com.novaco.luxapi.cobblemon.spawning

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import java.util.UUID

/**
 * Represents a temporary, localized mass outbreak (or "swarm") of a specific Pokémon species.
 * This data class holds all the configuration for a single swarm event, which is then
 * managed by the [SwarmManager].
 *
 * @property id A unique identifier for this swarm event.
 * @property speciesName The species of Pokémon that will appear in this swarm (e.g., "pikachu").
 * @property dimension The dimension in which the swarm is occurring.
 * @property centerPos The central block position around which the swarm is centered.
 * @property radius The radius (in blocks) from the center where Pokémon can spawn.
 * @property minLevel The minimum level of the Pokémon that will spawn.
 * @property maxLevel The maximum level of the Pokémon that will spawn.
 * @property maxActiveEntities The maximum number of Pokémon from this swarm that can be alive at any given time.
 * @property expireAt The system timestamp (in milliseconds) when this event should automatically end.
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
    /** A set of UUIDs for all Pokémon entities currently active as part of this swarm. */
    val activeEntities = mutableSetOf<UUID>()

    /**
     * Checks if the swarm event has passed its expiration time.
     * @return `true` if the event has expired, `false` otherwise.
     */
    fun isExpired(): Boolean = System.currentTimeMillis() > expireAt
}