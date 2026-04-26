package com.novaco.luxapi.cobblemon.boss.phase

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.event.BossPhaseChangeEvent
import com.novaco.luxapi.cobblemon.boss.event.LuxBossHooks
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a defined phase transition for a boss based on a health threshold.
 */
data class BossPhase(
    val healthThreshold: Float,
    val action: (PokemonEntity) -> Unit,
    var isTriggered: Boolean = false
)

/**
 * Manages phase transitions, evaluating health conditions and executing form changes dynamically.
 */
object BossPhaseManager {

    private val activePhases = ConcurrentHashMap<UUID, MutableList<BossPhase>>()

    /**
     * Registers a list of phases to a specific boss entity.
     */
    fun registerPhases(bossUuid: UUID, phases: List<BossPhase>) {
        activePhases[bossUuid] = phases.toMutableList()
    }

    /**
     * Unregisters all phases for a specific boss entity to free up memory.
     */
    fun unregister(bossUuid: UUID) {
        activePhases.remove(bossUuid)
    }

    /**
     * Evaluates the current health ratio and triggers any matching unactivated phases.
     */
    fun evaluatePhases(bossEntity: PokemonEntity, currentHpRatio: Float) {
        val phases = activePhases[bossEntity.uuid] ?: return

        phases.forEach { phase ->
            if (!phase.isTriggered && currentHpRatio <= phase.healthThreshold) {
                phase.isTriggered = true
                phase.action(bossEntity)
                LuxBossHooks.triggerPhaseChange(BossPhaseChangeEvent(bossEntity, phase.healthThreshold))
            }
        }
    }

    /**
     * Utility method to safely force a form change on a Pokemon entity during an active battle.
     */
    fun changeForm(bossEntity: PokemonEntity, formName: String) {
        val pokemon = bossEntity.pokemon
        PokemonProperties.parse("form=$formName").apply(pokemon)
    }
}