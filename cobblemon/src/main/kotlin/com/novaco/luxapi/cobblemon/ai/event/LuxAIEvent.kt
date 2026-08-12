package com.novaco.luxapi.cobblemon.ai.event

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.schedule.Activity

/**
 * Base abstract class for all LuxAPI artificial intelligence state transitions.
 */
sealed class LuxAIEvent {
    abstract val entity: LivingEntity

    /**
     * Fired when a Pokemon transitions from one brain activity pool to another.
     */
    data class ActivityChanged(
        override val entity: LivingEntity,
        val oldActivity: Activity?,
        val newActivity: Activity
    ) : LuxAIEvent()

    /**
     * Fired when a combat behavior targets a new entity (Aggro lock).
     */
    data class TargetAcquired(
        override val entity: LivingEntity,
        val target: LivingEntity
    ) : LuxAIEvent()
}