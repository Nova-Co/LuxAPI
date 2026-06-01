package com.novaco.luxapi.cobblemon.boss

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.phase.BossPhaseManager
import com.novaco.luxapi.core.bossbar.BossBarManager
import java.util.UUID

/**
 * Synchronizes a Pokemon's internal battle health with a global boss bar and the entity's physical health attribute.
 * This is crucial because a Pokemon's health in a Cobblemon battle is managed separately from the Minecraft entity's health.
 * This object ensures that the visual boss bar and the entity's health in the overworld accurately reflect the state of the battle.
 */
object BossHpSynchronizer {

    private val trackedBosses = mutableMapOf<UUID, PokemonEntity>()

    /**
     * Starts tracking a boss entity, linking its health updates to the BossBarManager.
     *
     * @param bossEntity The boss Pokemon entity to be tracked.
     */
    fun bindToBossBar(bossEntity: PokemonEntity) {
        trackedBosses[bossEntity.uuid] = bossEntity
    }

    /**
     * Stops tracking a boss entity, effectively unlinking it from the health synchronization process.
     *
     * @param uuid The UUID of the boss entity to unbind.
     */
    fun unbind(uuid: UUID) {
        trackedBosses.remove(uuid)
    }

    /**
     * The main update loop for the synchronizer. This method should be called on every server tick.
     * It iterates through all tracked bosses, updates their health, and handles cleanup for dead or removed entities.
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
     * Forces an immediate health synchronization for a specific boss.
     * It calculates the Pokemon's current health percentage, updates the boss bar, evaluates phase transitions,
     * and synchronizes the Minecraft entity's health attribute to match.
     *
     * @param bossEntity The boss Pokemon entity to synchronize.
     */
    fun syncHealth(bossEntity: PokemonEntity) {
        val pokemon = bossEntity.pokemon
        val maxHp = pokemon.maxHealth.toDouble()
        val currentHp = pokemon.currentHealth.toDouble()

        val progress = (currentHp / maxHp).coerceIn(0.0, 1.0).toFloat()

        // Check if a phase change should be triggered at the new health percentage
        BossPhaseManager.evaluatePhases(bossEntity, progress)

        // Push the accurate battle HP percentage to the Core UI
        BossBarManager.updateProgress(bossEntity.uuid, progress)

        // Synchronize the physical Minecraft entity health to match the battle health.
        // This ensures that overworld visual damage or direct hits respect the battle state.
        bossEntity.health = (bossEntity.maxHealth * progress)
    }
}