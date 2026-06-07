package com.novaco.luxapi.cobblemon.boss.phase

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.event.BossPhaseChangeEvent
import com.novaco.luxapi.cobblemon.boss.event.BossHooks
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a distinct phase of a boss fight, triggered at a specific health threshold.
 *
 * @property healthThreshold The health percentage (0.0 to 1.0) at which this phase should trigger.
 * @property action A lambda function defining the actions to be executed when this phase starts.
 * @property isTriggered A flag to ensure the phase action is only executed once.
 */
data class BossPhase(
    val healthThreshold: Float,
    val action: (PokemonEntity) -> Unit,
    var isTriggered: Boolean = false
)

/**
 * Manages the phase transitions for boss entities based on their health.
 * It tracks registered phases and executes their associated actions when health thresholds are met.
 */
object BossPhaseManager {

    // Map<BossUUID, List<BossPhase>>
    private val activePhases = ConcurrentHashMap<UUID, MutableList<BossPhase>>()

    /**
     * Registers a set of phases for a specific boss.
     *
     * @param bossUuid The UUID of the boss to which the phases will be attached.
     * @param phases A list of [BossPhase] objects defining the boss's phase transitions.
     */
    fun registerPhases(bossUuid: UUID, phases: List<BossPhase>) {
        activePhases[bossUuid] = phases.toMutableList()
    }

    /**
     * Removes all registered phases for a specific boss.
     * This should be called when the boss is defeated or despawns to prevent memory leaks.
     *
     * @param bossUuid The UUID of the boss whose phases should be unregistered.
     */
    fun unregister(bossUuid: UUID) {
        activePhases.remove(bossUuid)
    }

    /**
     * Evaluates the current health of a boss and triggers any corresponding phase changes.
     * It checks against all untriggered phases for the boss.
     *
     * @param bossEntity The boss entity whose health is being evaluated.
     * @param currentHpRatio The current health of the boss as a ratio (currentHP / maxHP).
     */
    fun evaluatePhases(bossEntity: PokemonEntity, currentHpRatio: Float) {
        val phases = activePhases[bossEntity.uuid] ?: return

        phases.forEach { phase ->
            if (!phase.isTriggered && currentHpRatio <= phase.healthThreshold) {
                phase.isTriggered = true
                phase.action(bossEntity)
                BossHooks.triggerPhaseChange(BossPhaseChangeEvent(bossEntity, phase.healthThreshold))
            }
        }
    }

    /**
     * A utility function to safely apply a form change to a Pokemon entity.
     *
     * @param bossEntity The Pokemon entity whose form is to be changed.
     * @param formName The name of the target form.
     */
    fun changeForm(bossEntity: PokemonEntity, formName: String) {
        val pokemon = bossEntity.pokemon
        PokemonProperties.parse("form=$formName").apply(pokemon)
    }
}