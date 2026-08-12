package com.novaco.luxapi.cobblemon.ai

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.schedule.Activity

/**
 * Represents a logical bundle of behavior tasks associated with their execution priorities
 * and target activity states. Used to define custom state routines.
 */
class LuxBehaviorGroup {

    private val taskEntries = mutableListOf<BehaviorEntry>()

    data class BehaviorEntry(
        val activity: Activity,
        val priority: Int,
        val task: BehaviorControl<in LivingEntity>
    )

    /**
     * Appends a targeted task into this behavior group configuration pool.
     */
    fun add(activity: Activity, priority: Int, task: BehaviorControl<in LivingEntity>): LuxBehaviorGroup {
        taskEntries.add(BehaviorEntry(activity, priority, task))
        return this
    }

    /**
     * Internal accessor to retrieve all planned behavior modifications.
     */
    fun getEntries(): List<BehaviorEntry> = taskEntries
}