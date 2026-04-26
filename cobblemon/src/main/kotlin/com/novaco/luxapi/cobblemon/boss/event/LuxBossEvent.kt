package com.novaco.luxapi.cobblemon.boss.event

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.UUID

/**
 * Represents the base event structure for all LuxAPI boss interactions.
 */
interface LuxBossEvent {
    val bossEntity: PokemonEntity
}

/**
 * Triggered immediately after a wild Pokemon is successfully transformed into a Lux Boss.
 */
data class BossSpawnEvent(
    override val bossEntity: PokemonEntity
) : LuxBossEvent

/**
 * Triggered when a boss reaches a specific health threshold and transitions to a new phase.
 */
data class BossPhaseChangeEvent(
    override val bossEntity: PokemonEntity,
    val healthThreshold: Float
) : LuxBossEvent

/**
 * Triggered when a boss is defeated, containing the sorted list of top damagers.
 */
data class BossDefeatEvent(
    override val bossEntity: PokemonEntity,
    val topDamagers: List<Pair<UUID, Double>>
) : LuxBossEvent