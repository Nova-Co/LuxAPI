package com.novaco.luxapi.cobblemon.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.phase.BossPhaseManager
import com.novaco.luxapi.core.bossbar.BossBarManager
import java.util.UUID

/**
 * API Tool to synchronize a Pokémon's internal Battle HP with the Global Boss Bar.
 * Overcomes the limitation where Minecraft entity health does not update instantly during turn-based battles.
 */
object BossHpSynchronizer {

    private val trackedBosses = mutableMapOf<UUID, PokemonEntity>()

    /**
     * Binds a Pokémon's health updates directly to the BossBarManager.
     * * @param bossEntity The World Boss entity.
     */
    fun bindToBossBar(bossEntity: PokemonEntity) {
        trackedBosses[bossEntity.uuid] = bossEntity
    }

    /**
     * Unbinds the entity from synchronization.
     * * @param uuid The UUID of the boss entity.
     */
    fun unbind(uuid: UUID) {
        trackedBosses.remove(uuid)
    }

    /**
     * Must be called every server tick.
     * Dynamically reads the true internal HP and updates the core Boss Bar for all bound bosses.
     */
    fun tick() {
        if (trackedBosses.isEmpty()) return

        val toRemove = mutableListOf<UUID>()

        for ((uuid, bossEntity) in trackedBosses) {
            if (!bossEntity.isAlive || bossEntity.isRemoved) {
                toRemove.add(uuid)
                continue
            }
            syncHealth(bossEntity)
        }

        toRemove.forEach { unbind(it) }
    }

    /**
     * Manually forces a health synchronization for a specific boss.
     * Calculates the internal Pokémon HP percentage and pushes it to the generic BossBarManager.
     * * @param bossEntity The World Boss entity.
     */
    fun syncHealth(bossEntity: PokemonEntity) {
        val pokemon = bossEntity.pokemon
        val maxHp = pokemon.maxHealth.toDouble()
        val currentHp = pokemon.currentHealth.toDouble()

        val progress = (currentHp / maxHp).coerceIn(0.0, 1.0).toFloat()

        BossPhaseManager.evaluatePhases(bossEntity, progress)

        // Push the accurate battle HP percentage to the Core UI
        BossBarManager.updateProgress(bossEntity.uuid, progress)

        // Synchronize the physical Minecraft entity health to match the battle health.
        // Ensures that overworld visual damage or direct hits respect the battle state.
        bossEntity.health = (bossEntity.maxHealth * progress)
    }
}