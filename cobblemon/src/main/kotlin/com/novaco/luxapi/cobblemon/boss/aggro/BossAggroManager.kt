package com.novaco.luxapi.cobblemon.boss.aggro

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks player damage and calculates aggro to determine the primary target for boss AI.
 */
object BossAggroManager {

    // Map<BossUUID, Map<PlayerUUID, AggroValue>>
    private val aggroTables = ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Double>>()

    /**
     * Adds aggro (damage or threat) for a specific player against a boss.
     */
    fun addAggro(bossEntity: PokemonEntity, player: ServerPlayer, amount: Double) {
        val bossTable = aggroTables.getOrPut(bossEntity.uuid) { ConcurrentHashMap() }
        val currentAggro = bossTable.getOrDefault(player.uuid, 0.0)
        bossTable[player.uuid] = currentAggro + amount
    }

    /**
     * Reduces the aggro of a specific player (e.g., when they use a stealth skill or die).
     */
    fun reduceAggro(bossEntity: PokemonEntity, player: ServerPlayer, amount: Double) {
        val bossTable = aggroTables[bossEntity.uuid] ?: return
        val currentAggro = bossTable.getOrDefault(player.uuid, 0.0)
        val newAggro = (currentAggro - amount).coerceAtLeast(0.0)
        bossTable[player.uuid] = newAggro
    }

    /**
     * Retrieves the player with the highest aggro for a given boss.
     */
    fun getTopTarget(bossEntity: PokemonEntity): ServerPlayer? {
        val bossTable = aggroTables[bossEntity.uuid] ?: return null
        val server = bossEntity.server ?: return null

        val topPlayerUuid = bossTable.maxByOrNull { it.value }?.key ?: return null
        return server.playerList.getPlayer(topPlayerUuid)
    }

    /**
     * Returns a sorted list of players based on their aggro towards the boss.
     */
    fun getTopDamagers(bossEntity: PokemonEntity): List<Pair<UUID, Double>> {
        val bossTable = aggroTables[bossEntity.uuid] ?: return emptyList()
        return bossTable.entries.map { it.key to it.value }.sortedByDescending { it.second }
    }

    /**
     * Clears the aggro table for a specific boss when it is defeated or despawns.
     */
    fun clearAggro(bossUuid: UUID) {
        aggroTables.remove(bossUuid)
    }
}