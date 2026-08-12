package com.novaco.luxapi.cobblemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.CobblemonAttackTargetData
import com.cobblemon.mod.common.api.ai.config.task.SingleTaskConfig
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.asExpression
import com.novaco.luxapi.cobblemon.ai.event.LuxAIEventListener
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.sensing.SensorType
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * A specialized wrapper providing continuous waypoint patrolling for guardian or wild Pokemon.
 * Switches seamlessly to target acquisition when an eligible entity enters its detection range.
 */
class PatrolTaskWrapper(
    private val waypoints: List<BlockPos>,
    private val targetCondition: String = "true",
    private val scanRange: Float = 16.0f,
    private val movementSpeed: Float = 0.4f
) : SingleTaskConfig {

    override fun getVariables(entity: LivingEntity, context: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()

    override fun createTask(entity: LivingEntity, context: BehaviourConfigurationContext): BehaviorControl<in LivingEntity>? {
        context.addMemories(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            CobblemonMemories.ATTACK_TARGET_DATA
        )
        context.addSensors(SensorType.NEAREST_LIVING_ENTITIES)

        var currentWaypointIndex = 0

        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES),
                instance.registered(MemoryModuleType.ATTACK_TARGET),
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.registered(MemoryModuleType.LOOK_TARGET)
            ).apply(instance) { entities, attackTarget, walkTarget, lookTarget ->
                Trigger { world, mob, gameTime ->
                    // 1. Scan for threat targets first
                    val threat = instance.get(entities).findClosest {
                        mob.distanceTo(it) <= scanRange
                    }.orElse(null)

                    if (threat != null) {
                        // Securely bypass MoLang class visibility restrictions using runtime reflection layer
                        val targetDataObj = try {
                            // Locate the native String extension function from Cobblemon utility layer
                            val extensionsClass = Class.forName("com.cobblemon.mod.common.util.MoLangExtensionsKt")
                            val asExpressionMethod: Method = extensionsClass.getMethod("asExpression", String::class.java)

                            // Parse targetCondition string into a native Expression object at runtime
                            val targetExpressionObj = asExpressionMethod.invoke(null, targetCondition)

                            // Extract constructor signature for CobblemonAttackTargetData
                            val targetDataClass = Class.forName("com.cobblemon.mod.common.api.ai.CobblemonAttackTargetData")
                            val expressionClass = Class.forName("com.bedrockk.molang.Expression")
                            val kotlinFunction1 = Class.forName("kotlin.jvm.functions.Function1")

                            val constructor: Constructor<*> = targetDataClass.getConstructor(expressionClass, Int::class.java, kotlinFunction1)

                            // Instantiate data contract with default disengage conditions
                            val defaultDisengageAction: (net.minecraft.world.entity.Entity) -> Unit = {}
                            constructor.newInstance(targetExpressionObj, -1, defaultDisengageAction)
                        } catch (t: Throwable) {
                            println("[LuxAPI | AI Reflection Alert] Failed to construct CobblemonAttackTargetData safely: ${t.message}")
                            null
                        }

                        // Safely cast the generic reflection Any object to the expected memory data block representation
                        val targetDataInstance = targetDataObj as? CobblemonAttackTargetData
                        if (targetDataInstance != null) {
                            mob.brain.setMemory(CobblemonMemories.ATTACK_TARGET_DATA, targetDataInstance)
                        }

                        attackTarget.set(threat)
                        LuxAIEventListener.postTargetAcquired(mob, threat)
                        return@Trigger true
                    }

                    // 2. Resume patrol routing if no threats detected
                    if (waypoints.isEmpty()) return@Trigger false

                    val activeGoal = waypoints[currentWaypointIndex]
                    if (mob.blockPosition().closerThan(activeGoal, 2.0)) {
                        currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size
                    }

                    val nextPos = waypoints[currentWaypointIndex]

                    // Directly utilize Vanilla Minecraft Behavior structures for guaranteed classpath matching
                    walkTarget.set(WalkTarget(nextPos, movementSpeed, 1))
                    lookTarget.set(BlockPosTracker(nextPos))
                    return@Trigger true
                }
            }
        }
    }
}