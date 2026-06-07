package com.novaco.luxapi.cobblemon.boss.event

import java.util.concurrent.CopyOnWriteArrayList

/**
 * A centralized event bus for hooking into the lifecycle of LuxAPI boss encounters.
 * This object provides methods for subscribing to various boss events and is responsible
 * for dispatching those events to all registered listeners.
 */
object BossHooks {

    private val spawnListeners = CopyOnWriteArrayList<(BossSpawnEvent) -> Unit>()
    private val phaseChangeListeners = CopyOnWriteArrayList<(BossPhaseChangeEvent) -> Unit>()
    private val defeatListeners = CopyOnWriteArrayList<(BossDefeatEvent) -> Unit>()

    /**
     * Registers a listener that will be executed when a boss spawns.
     *
     * @param action The code block to execute with the [BossSpawnEvent].
     */
    fun onBossSpawned(action: (BossSpawnEvent) -> Unit) {
        spawnListeners.add(action)
    }

    /**
     * Registers a listener that will be executed when a boss changes its phase.
     *
     * @param action The code block to execute with the [BossPhaseChangeEvent].
     */
    fun onPhaseChanged(action: (BossPhaseChangeEvent) -> Unit) {
        phaseChangeListeners.add(action)
    }

    /**
     * Registers a listener that will be executed when a boss is defeated.
     *
     * @param action The code block to execute with the [BossDefeatEvent].
     */
    fun onBossDefeated(action: (BossDefeatEvent) -> Unit) {
        defeatListeners.add(action)
    }

    /**
     * Internal function to dispatch a [BossSpawnEvent] to all subscribed listeners.
     * This should only be called by the LuxAPI's internal boss management system.
     *
     * @param event The [BossSpawnEvent] to be triggered.
     */
    internal fun triggerSpawn(event: BossSpawnEvent) {
        spawnListeners.forEach { it.invoke(event) }
    }

    /**
     * Internal function to dispatch a [BossPhaseChangeEvent] to all subscribed listeners.
     * This should only be called by the LuxAPI's internal boss management system.
     *
     * @param event The [BossPhaseChangeEvent] to be triggered.
     */
    internal fun triggerPhaseChange(event: BossPhaseChangeEvent) {
        phaseChangeListeners.forEach { it.invoke(event) }
    }

    /**
     * Internal function to dispatch a [BossDefeatEvent] to all subscribed listeners.
     * This should only be called by the LuxAPI's internal boss management system.
     *
     * @param event The [BossDefeatEvent] to be triggered.
     */
    internal fun triggerDefeat(event: BossDefeatEvent) {
        defeatListeners.forEach { it.invoke(event) }
    }
}