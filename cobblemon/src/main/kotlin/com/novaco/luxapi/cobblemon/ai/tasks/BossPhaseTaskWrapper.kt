package com.novaco.luxapi.cobblemon.ai.tasks

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.config.task.SingleTaskConfig
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import java.lang.reflect.Method

/**
 * A dynamic boss mechanics wrapper that alternates combat attributes (speed, range, cooldown)
 * based on the target entity's health percentage thresholds.
 */
class BossPhaseTaskWrapper(
    private val healthThreshold: Float = 0.5f,
    private val standardSpeed: Float = 0.5f,
    private val enragedSpeed: Float = 0.75f,
    private val standardCooldown: Int = 30,
    private val enragedCooldown: Int = 15
) : SingleTaskConfig {

    override fun getVariables(entity: LivingEntity, context: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()

    override fun createTask(entity: LivingEntity, context: BehaviourConfigurationContext): BehaviorControl<in LivingEntity>? {
        context.addMemories(MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN)

        val normalMoveTask = safeCreateMoveTask(standardSpeed, 0) as? net.minecraft.world.entity.ai.behavior.OneShot<LivingEntity>
        val enragedMoveTask = safeCreateMoveTask(enragedSpeed, 0) as? net.minecraft.world.entity.ai.behavior.OneShot<LivingEntity>
        val normalAttackTask = safeCreateAttackTask(0.75f, standardCooldown) as? net.minecraft.world.entity.ai.behavior.OneShot<LivingEntity>
        val enragedAttackTask = safeCreateAttackTask(1.2f, enragedCooldown) as? net.minecraft.world.entity.ai.behavior.OneShot<LivingEntity>

        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET)
            ).apply(instance) { _ ->
                Trigger { world, boss, gameTime ->
                    val healthPercentage = boss.health / boss.maxHealth
                    val isEnraged = healthPercentage <= healthThreshold

                    // Redirect dynamic execution routines inside tick cycles
                    if (isEnraged) {
                        enragedMoveTask?.tryStart(world, boss, gameTime)
                        enragedAttackTask?.tryStart(world, boss, gameTime)
                    } else {
                        normalMoveTask?.tryStart(world, boss, gameTime)
                        normalAttackTask?.tryStart(world, boss, gameTime)
                    }
                    return@Trigger true
                }
            }
        }
    }

    /**
     * Creates a MoveToAttackTargetTask instance safely without exposing or accessing MoLang Expression classes directly.
     * Dynamically resolves internal MoLang factory nodes via runtime reflection.
     * Provided by Nova Co. Core AI Project Companion.
     */
    fun safeCreateMoveTask(speedMultiplier: Float, closeEnoughDistance: Int): Any? {
        return try {
            // 1. Locate the native String extension function from Cobblemon utility layer
            val extensionsClass = Class.forName("com.cobblemon.mod.common.util.MoLangExtensionsKt")
            val asExpressionMethod: Method = extensionsClass.getMethod("asExpression", String::class.java)

            // 2. Parse raw primitive numbers into native Expression objects behind the scenes
            val speedExpr = asExpressionMethod.invoke(null, speedMultiplier.toString())
            val distanceExpr = asExpressionMethod.invoke(null, closeEnoughDistance.toString())

            // 3. Dynamically invoke the native MoveToAttackTargetTask factory node
            val taskClass = Class.forName("com.cobblemon.mod.common.entity.ai.MoveToAttackTargetTask")
            val createMethod = taskClass.methods.first { it.name == "create" }

            createMethod.invoke(null, speedExpr, distanceExpr)
        } catch (t: Throwable) {
            println("[LuxAPI | AI Error] Failed to safely compile MoveToAttackTargetTask: ${t.message}")
            null
        }
    }

    /**
     * Creates a MeleeAttackTask instance safely without exposing or accessing MoLang Expression classes directly.
     * Dynamically resolves internal MoLang factory nodes via runtime reflection.
     * Provided by Nova Co. Core AI Project Companion.
     */
    fun safeCreateAttackTask(range: Float, cooldownTicks: Int): Any? {
        return try {
            val extensionsClass = Class.forName("com.cobblemon.mod.common.util.MoLangExtensionsKt")
            val asExpressionMethod: Method = extensionsClass.getMethod("asExpression", String::class.java)

            val rangeExpr = asExpressionMethod.invoke(null, range.toString())
            val cooldownExpr = asExpressionMethod.invoke(null, cooldownTicks.toString())

            val taskClass = Class.forName("com.cobblemon.mod.common.entity.npc.ai.MeleeAttackTask")
            val createMethod = taskClass.methods.first { it.name == "create" }

            createMethod.invoke(null, rangeExpr, cooldownExpr)
        } catch (t: Throwable) {
            println("[LuxAPI | AI Error] Failed to safely compile MeleeAttackTask: ${t.message}")
            null
        }
    }
}