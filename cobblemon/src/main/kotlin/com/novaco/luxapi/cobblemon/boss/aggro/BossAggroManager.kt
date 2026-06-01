package com.novaco.luxapi.cobblemon.boss.aggro

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages aggro (threat) levels of players towards boss entities.
 * This is used to determine the boss's target in a multi-player battle scenario.
 */
object BossAggroManager {

    // Map<BossUUID, Map<PlayerUUID, AggroValue>>
    private val aggroTables = ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Double>>()

    /**
     * Adds a specified amount of aggro for a player towards a boss.
     * This is typically called when a player deals damage or uses a threat-generating ability.
     *
     * @param bossEntity The boss Pokemon entity.
     * @param player The player generating the aggro.
     * @param amount The amount of aggro to add.
     */
    fun addAggro(bossEntity: PokemonEntity, player: ServerPlayer, amount: Double) {
        val bossTable = aggroTables.getOrPut(bossEntity.uuid) { ConcurrentHashMap() }
        val currentAggro = bossTable.getOrDefault(player.uuid, 0.0)
        bossTable[player.uuid] = currentAggro + amount
    }

    /**
     * Reduces a player's aggro towards a boss by a specified amount.
     * This can be used for abilities that reduce threat or upon player death.
     *
     * @param bossEntity The boss Pokemon entity.
     * @param player The player whose aggro is to be reduced.
     * @param amount The amount of aggro to reduce.
     */
    fun reduceAggro(bossEntity: PokemonEntity, player: ServerPlayer, amount: Double) {
        val bossTable = aggroTables[bossEntity.uuid] ?: return
        val currentAggro = bossTable.getOrDefault(player.uuid, 0.0)
        val newAggro = (currentAggro - amount).coerceAtLeast(0.0)
        bossTable[player.uuid] = newAggro
    }

    /**
     * Determines and returns the player with the highest aggro for a specific boss.
     * This player should be the primary target for the boss's attacks.
     *
     * @param bossEntity The boss Pokemon entity.
     * @return The ServerPlayer with the highest aggro, or null if no player has aggro.
     */
    fun getTopTarget(bossEntity: PokemonEntity): ServerPlayer? {
        val bossTable = aggroTables[bossEntity.uuid] ?: return null
        val server = bossEntity.server ?: return null

        val topPlayerUuid = bossTable.maxByOrNull { it.value }?.key ?: return null
        return server.playerList.getPlayer(topPlayerUuid)
    }

    /**
     * Retrieves a sorted list of players based on their aggro value, from highest to lowest.
     * This can be used for displaying damage meters or for mechanics based on aggro ranking.
     *
     * @param bossEntity The boss Pokemon entity.
     * @return A list of pairs, each containing a player's UUID and their aggro value, sorted in descending order.
     */
    fun getTopDamagers(bossEntity: PokemonEntity): List<Pair<UUID, Double>> {
        val bossTable = aggroTables[bossEntity.uuid] ?: return emptyList()
        return bossTable.entries.map { it.key to it.value }.sortedByDescending { it.second }
    }

    /**
     * Clears all aggro data associated with a specific boss.
     * This should be called when the boss is defeated, despawns, or the encounter resets.
     *
     * @param bossUuid The UUID of the boss whose aggro table should be cleared.
     */
    fun clearAggro(bossUuid: UUID) {
        aggroTables.remove(bossUuid)
    }
}