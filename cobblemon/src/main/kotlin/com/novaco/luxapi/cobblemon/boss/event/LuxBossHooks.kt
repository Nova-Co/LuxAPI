package com.novaco.luxapi.cobblemon.boss.event

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Centralized registry for developers to hook into the boss lifecycle.
 * Provides subscription methods and internal trigger mechanisms.
 */
object LuxBossHooks {

    private val spawnListeners = CopyOnWriteArrayList<(BossSpawnEvent) -> Unit>()
    private val phaseChangeListeners = CopyOnWriteArrayList<(BossPhaseChangeEvent) -> Unit>()
    private val defeatListeners = CopyOnWriteArrayList<(BossDefeatEvent) -> Unit>()

    /**
     * Subscribes a listener to the boss spawn event.
     */
    fun onBossSpawned(action: (BossSpawnEvent) -> Unit) {
        spawnListeners.add(action)
    }

    /**
     * Subscribes a listener to the boss phase transition event.
     */
    fun onPhaseChanged(action: (BossPhaseChangeEvent) -> Unit) {
        phaseChangeListeners.add(action)
    }

    /**
     * Subscribes a listener to the boss defeat event.
     */
    fun onBossDefeated(action: (BossDefeatEvent) -> Unit) {
        defeatListeners.add(action)
    }

    /**
     * Internally triggers the spawn event to all registered listeners.
     */
    internal fun triggerSpawn(event: BossSpawnEvent) {
        spawnListeners.forEach { it.invoke(event) }
    }

    /**
     * Internally triggers the phase change event to all registered listeners.
     */
    internal fun triggerPhaseChange(event: BossPhaseChangeEvent) {
        phaseChangeListeners.forEach { it.invoke(event) }
    }

    /**
     * Internally triggers the defeat event to all registered listeners.
     */
    internal fun triggerDefeat(event: BossDefeatEvent) {
        defeatListeners.forEach { it.invoke(event) }
    }
}