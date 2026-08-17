package com.novaco.luxapi.cobblemon.boss.reward

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * Data object holding the configured rewards for a specific boss.
 */
class BossRewardPool(
    val bossId: String,
    val minDamageThreshold: Double,
    val exactRankRewards: Map<Int, (ServerPlayer) -> Unit>,
    val rangeRankRewards: Map<IntRange, (ServerPlayer) -> Unit>,
    val participationReward: ((ServerPlayer) -> Unit)?,
    val autoDistribute: Boolean = true
) {
    /**
     * Distributes the rewards to eligible players based on their damage ranking.
     * 
     * @param bossEntity The boss entity that was defeated.
     * @param topDamagers The list of players and their total damage dealt.
     */
    fun distribute(bossEntity: PokemonEntity, topDamagers: List<Pair<UUID, Double>>) {
        val server = bossEntity.server ?: return

        // Filter out players who haven't met the minimum damage threshold.
        val eligibleDamagers = topDamagers.filter { it.second >= minDamageThreshold }

        eligibleDamagers.forEachIndexed { index, (uuid, _) ->
            val player = server.playerList.getPlayer(uuid) ?: return@forEachIndexed
            val rank = index + 1 // Rank starts from 1

            // Give rewards for exact rank match (e.g., top 1)
            exactRankRewards[rank]?.invoke(player)

            // Give rewards for rank range match (e.g., top 2-5)
            rangeRankRewards.forEach { (range, action) ->
                if (rank in range) {
                    action.invoke(player)
                }
            }

            // Give participation reward to everyone who passed the damage threshold
            participationReward?.invoke(player)
        }
    }
}

/**
 * Fluent DSL Builder for creating a [BossRewardPool].
 */
class LuxBossRewardBuilder(private val bossId: String) {

    /** The minimum damage a player must deal to be eligible for rewards. */
    var minimumDamageThreshold: Double = 0.0

    /**
     * When false, the defeat hook will NOT auto-resolve/grant rewards for this pool.
     * Consumers must call [BossRewardManager.distributeTo] or [BossRewardManager.distributeRaidRewards] manually.
     */
    var autoDistribute: Boolean = true

    private val exactRankRewards = mutableMapOf<Int, (ServerPlayer) -> Unit>()
    private val rangeRankRewards = mutableMapOf<IntRange, (ServerPlayer) -> Unit>()
    private var participationRewardAction: ((ServerPlayer) -> Unit)? = null

    /** 
     * Sets the reward for a specific exact rank (e.g., 1st place).
     * 
     * @param rank The exact rank to receive this reward.
     * @param action The reward action to execute.
     */
    fun topDamager(rank: Int, action: (ServerPlayer) -> Unit) {
        exactRankRewards[rank] = action
    }

    /** 
     * Sets the reward for a specific range of ranks (e.g., 2nd to 5th place).
     * 
     * @param rankRange The range of ranks to receive this reward.
     * @param action The reward action to execute.
     */
    fun topDamagers(rankRange: IntRange, action: (ServerPlayer) -> Unit) {
        rangeRankRewards[rankRange] = action
    }

    /** 
     * Sets the reward for any player who participated and met the minimum damage threshold.
     * 
     * @param action The reward action to execute.
     */
    fun participationReward(action: (ServerPlayer) -> Unit) {
        participationRewardAction = action
    }

    /**
     * Builds and returns the configured BossRewardPool.
     */
    internal fun build(): BossRewardPool {
        return BossRewardPool(
            bossId = bossId,
            minDamageThreshold = minimumDamageThreshold,
            exactRankRewards = exactRankRewards.toMap(),
            rangeRankRewards = rangeRankRewards.toMap(),
            participationReward = participationRewardAction,
            autoDistribute = autoDistribute
        )
    }
}