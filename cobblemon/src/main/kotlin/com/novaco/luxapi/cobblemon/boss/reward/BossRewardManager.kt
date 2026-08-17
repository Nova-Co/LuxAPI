package com.novaco.luxapi.cobblemon.boss.reward

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.aggro.BossAggroManager
import com.novaco.luxapi.cobblemon.boss.event.BossHooks
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Manages the registration and execution of boss reward pools.
 */
object BossRewardManager {

    private val rewardPools = mutableMapOf<String, BossRewardPool>()
    private val interceptors = CopyOnWriteArrayList<(RewardDistributionEvent) -> Unit>()

    /**
     * Fired before a reward pool's grants are resolved, whether triggered automatically
     * on boss defeat or via a manual [distributeTo]/[distributeRaidRewards] call.
     * Set [RewardDistributionEvent.cancelled] to true to skip the grant for this call.
     *
     * @property poolId The reward pool being resolved.
     * @property bossEntity The boss entity involved, if known (null for [distributeTo]).
     * @property players The players eligible to receive rewards from this call.
     */
    class RewardDistributionEvent(
        val poolId: String,
        val bossEntity: PokemonEntity?,
        val players: List<ServerPlayer>
    ) {
        var cancelled: Boolean = false
    }

    /**
     * Opens a DSL builder to create and register a reward pool for a specific boss ID.
     *
     * @param bossId The unique identifier of the boss (matches "lux_boss_id" tag).
     * @param block The configuration block.
     */
    fun createPool(bossId: String, block: LuxBossRewardBuilder.() -> Unit) {
        val builder = LuxBossRewardBuilder(bossId)
        builder.block()
        rewardPools[bossId] = builder.build()
    }

    /**
     * Registers a listener invoked before rewards are granted for any pool, on both the
     * automatic defeat path and manual distribution calls.
     *
     * @param action The code block to execute with the [RewardDistributionEvent].
     */
    fun onBeforeRewardDistributed(action: (RewardDistributionEvent) -> Unit) {
        interceptors.add(action)
    }

    /**
     * Resolves and grants a single player's participation reward for the given pool.
     * Rank-based (exact/range) rewards are not evaluated here since no boss/damage
     * context is available — use [distributeRaidRewards] for tiered payouts.
     *
     * @param player The player to grant the reward to.
     * @param poolId The reward pool to resolve against.
     */
    fun distributeTo(player: ServerPlayer, poolId: String) {
        val pool = rewardPools[poolId] ?: return

        val event = RewardDistributionEvent(poolId, null, listOf(player))
        interceptors.forEach { it.invoke(event) }
        if (event.cancelled) return

        pool.participationReward?.invoke(player)
    }

    /**
     * Resolves and distributes tiered/participation rewards to all eligible raid
     * participants for the given boss, based on the current damage-aggro standings.
     *
     * @param bossEntity The boss entity whose damage standings should be used.
     * @param poolId The reward pool to resolve against.
     */
    fun distributeRaidRewards(bossEntity: PokemonEntity, poolId: String) {
        val pool = rewardPools[poolId] ?: return
        val topDamagers = BossAggroManager.getTopDamagers(bossEntity)

        val server = bossEntity.server
        val players = if (server != null) {
            topDamagers.mapNotNull { (uuid, _) -> server.playerList.getPlayer(uuid) }
        } else {
            emptyList()
        }

        val event = RewardDistributionEvent(poolId, bossEntity, players)
        interceptors.forEach { it.invoke(event) }
        if (event.cancelled) return

        pool.distribute(bossEntity, topDamagers)
    }

    /**
     * Initializes the manager by subscribing to the Boss Defeat Hook.
     * This should be called during the plugin/mod initialization phase.
     */
    fun register() {
        BossHooks.onBossDefeated { event ->
            val bossEntity = event.bossEntity

            // Extract the boss ID from the tags
            val bossIdTag = bossEntity.tags.find { it.startsWith("lux_boss_id:") }
            val bossId = bossIdTag?.substringAfter("lux_boss_id:") ?: return@onBossDefeated

            // Only auto-resolve pools that opted into automatic distribution.
            val pool = rewardPools[bossId] ?: return@onBossDefeated
            if (!pool.autoDistribute) return@onBossDefeated

            distributeRaidRewards(bossEntity, bossId)
        }
    }
}
