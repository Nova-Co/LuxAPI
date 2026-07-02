package com.novaco.luxapi.cobblemon.ai

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.schedule.Activity

/**
 * High-level manager providing an intuitive API for developers to assign prioritized actions
 * and customized conditions to wild or boss Pokemon.
 */
class CustomGoalSelector(
    private val entity: LivingEntity,
    private val context: BehaviourConfigurationContext
) {

    /**
     * Injects a raw or wrapped BehaviorControl into a specific activity state with a designated execution priority.
     * * @param activity The target activity state (e.g., Activity.IDLE, Activity.FIGHT).
     * @param priority The execution priority layer. Lower numerical values execute first.
     * @param task The custom behavior control framework instance.
     */
    fun addBehavior(activity: Activity, priority: Int, task: BehaviorControl<in LivingEntity>) {
        val activityContext = context.getOrCreateActivity(activity)
        activityContext.addTasks(priority, task)
    }

    /**
     * Helper to inject built custom behaviors directly through the CustomAIBuilder wrapper pipeline.
     * * @param activity The target activity state where this routine belongs.
     * @param priority The execution priority layer.
     * @param builder The configured CustomAIBuilder configuration instance.
     */
    fun addBuiltBehavior(activity: Activity, priority: Int, builder: CustomAIBuilder) {
        val builtTask = builder.build(entity, context)
        if (builtTask != null) {
            addBehavior(activity, priority, builtTask)
        }
    }
}