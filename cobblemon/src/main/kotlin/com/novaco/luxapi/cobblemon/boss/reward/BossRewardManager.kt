package com.novaco.luxapi.cobblemon.boss.reward

import com.novaco.luxapi.cobblemon.boss.event.BossHooks

/**
 * Manages the registration and execution of boss reward pools.
 */
object BossRewardManager {

    private val rewardPools = mutableMapOf<String, BossRewardPool>()

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
     * Initializes the manager by subscribing to the Boss Defeat Hook.
     * This should be called during the plugin/mod initialization phase.
     */
    fun register() {
        BossHooks.onBossDefeated { event ->
            val bossEntity = event.bossEntity

            // Extract the boss ID from the tags
            val bossIdTag = bossEntity.tags.find { it.startsWith("lux_boss_id:") }
            val bossId = bossIdTag?.substringAfter("lux_boss_id:") ?: return@onBossDefeated

            // If there is a reward pool associated with this boss, process and distribute the rewards
            rewardPools[bossId]?.distribute(bossEntity, event.topDamagers)
        }
    }
}