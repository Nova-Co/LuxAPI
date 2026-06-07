package com.novaco.luxapi.cobblemon.boss.event

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.UUID

/**
 * A base interface for all events related to LuxAPI boss encounters.
 * It ensures that every boss event provides access to the associated boss entity.
 */
interface LuxBossEvent {
    /** The Pokemon entity designated as the boss for this event. */
    val bossEntity: PokemonEntity
}

/**
 * Fired when a wild Pokemon is successfully converted into a Lux Boss.
 * This event marks the beginning of a boss encounter.
 *
 * @property bossEntity The newly created boss Pokemon entity.
 */
data class BossSpawnEvent(
    override val bossEntity: PokemonEntity
) : LuxBossEvent

/**
 * Fired when a boss's health drops to a predefined threshold, triggering a phase change.
 * This allows for dynamic changes in the boss's behavior, abilities, or mechanics during the fight.
 *
 * @property bossEntity The boss Pokemon entity changing phase.
 * @property healthThreshold The health percentage that triggered this phase change.
 */
data class BossPhaseChangeEvent(
    override val bossEntity: PokemonEntity,
    val healthThreshold: Float
) : LuxBossEvent

/**
 * Fired when a boss is defeated.
 * This event provides a summary of the encounter, including a list of players who dealt the most damage.
 *
 * @property bossEntity The boss Pokemon entity that was defeated.
 * @property topDamagers A sorted list of pairs, where each pair contains a player's UUID and their total damage dealt.
 */
data class BossDefeatEvent(
    override val bossEntity: PokemonEntity,
    val topDamagers: List<Pair<UUID, Double>>
) : LuxBossEvent