package com.novaco.luxapi.cobblemon.ai

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

/**
 * High-level builder to easily construct and inject custom tasks, sensors,
 * and memories into a Pokemon's brain without diving into raw vanilla BehaviorBuilders.
 * Used by developers implementing the LuxAPI ecosystem.
 */
class CustomAIBuilder {

    private val requiredMemories = mutableListOf<MemoryModuleType<*>>()
    private val requiredSensors = mutableListOf<SensorType<*>>()
    private var taskFactory: ((LivingEntity, BehaviourConfigurationContext) -> BehaviorControl<in LivingEntity>?)? = null

    /**
     * Declares memory modules that this custom behavior requires to operate.
     */
    fun requireMemories(vararg memories: MemoryModuleType<*>): CustomAIBuilder {
        this.requiredMemories.addAll(memories)
        return this
    }

    /**
     * Declares sensor types needed to scan the environment for this behavior.
     */
    fun requireSensors(vararg sensors: SensorType<*>): CustomAIBuilder {
        this.requiredSensors.addAll(sensors)
        return this
    }

    /**
     * Defines the execution logic of the behavior using an instance of the target entity.
     */
    fun executes(factory: (LivingEntity, BehaviourConfigurationContext) -> BehaviorControl<in LivingEntity>?): CustomAIBuilder {
        this.taskFactory = factory
        return this
    }

    /**
     * Builds and appends the custom configuration into the Cobblemon context pipeline.
     */
    fun build(entity: LivingEntity, context: BehaviourConfigurationContext): BehaviorControl<in LivingEntity>? {
        if (requiredMemories.isNotEmpty()) {
            context.addMemories(*requiredMemories.toTypedArray())
        }
        if (requiredSensors.isNotEmpty()) {
            context.addSensors(*requiredSensors.toTypedArray())
        }
        return taskFactory?.invoke(entity, context)
    }
}