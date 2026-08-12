package com.novaco.luxapi.cobblemon.ai.event

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.schedule.Activity
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Central event broker allowing decoupled modules (Quest systems, Web Tech, Webhooks)
 * to monitor live Pokemon intelligence changes without modifying core AI behaviors.
 */
object LuxAIEventListener {

    private val activityListeners = CopyOnWriteArrayList<(LuxAIEvent.ActivityChanged) -> Unit>()
    private val targetListeners = CopyOnWriteArrayList<(LuxAIEvent.TargetAcquired) -> Unit>()

    /**
     * Subscribes an observer lambda to monitor state activity changes.
     */
    @JvmStatic
    fun subscribeToActivity(listener: (LuxAIEvent.ActivityChanged) -> Unit) {
        activityListeners.add(listener)
    }

    /**
     * Subscribes an observer lambda to detect threat targeting hooks.
     */
    @JvmStatic
    fun subscribeToTarget(listener: (LuxAIEvent.TargetAcquired) -> Unit) {
        targetListeners.add(listener)
    }

    /**
     * Internal framework hook called inside custom wrappers to dispatch event changes.
     */
    internal fun postActivityChange(entity: LivingEntity, old: Activity?, new: Activity) {
        val event = LuxAIEvent.ActivityChanged(entity, old, new)
        activityListeners.forEach { it.invoke(event) }
    }

    /**
     * Internal framework hook called inside custom wrappers to dispatch tracking state changes.
     */
    internal fun postTargetAcquired(entity: LivingEntity, target: LivingEntity) {
        val event = LuxAIEvent.TargetAcquired(entity, target)
        targetListeners.forEach { it.invoke(event) }
    }
}