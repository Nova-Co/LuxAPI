package com.novaco.luxapi.cobblemon.ai

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import net.minecraft.world.entity.LivingEntity
import org.slf4j.LoggerFactory

/**
 * Core runtime injection utility designed to dynamically mount LuxAPI behaviors
 * into live Pokemon entity brain instances bypassing static asset configurations.
 */
object LuxAIInjector {

    private val logger = LoggerFactory.getLogger(LuxAIInjector::class.java)

    /**
     * Force injects a group of predefined custom behaviors directly into an entity's internal brain.
     * Useful during entity spawn events, quest triggers, or live boss transitions.
     * * @param entity The targeted LivingEntity instance (e.g., PokemonEntity).
     * @param context The current operational behavior context framework instance from Cobblemon.
     * @param behaviorGroup The predefined bundle of custom tasks to deploy.
     */
    @JvmStatic
    fun inject(entity: LivingEntity, context: BehaviourConfigurationContext, behaviorGroup: LuxBehaviorGroup) {
        behaviorGroup.getEntries().forEach { entry ->
            // Replicates Cobblemon's AddTasksToActivity logic cleanly via context pipeline manipulation
            val activityContext = context.getOrCreateActivity(entry.activity)
            activityContext.addTasks(entry.priority, entry.task)
        }

        // Re-ignite or refresh brain activities if the entity is already running active ticks
        // The engine natively handles empty schedule validation internally within the update pipeline.
        try {
            entity.brain.updateActivityFromSchedule(
                entity.level().dayTime(),
                entity.level().gameTime
            )
        } catch (e: NullPointerException) {
            logger.warn("Failed to update brain for entity #{}: world is null or unloaded.", entity.id)
        }
    }
}