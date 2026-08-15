package com.novaco.luxapi.core.effect

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

/**
 * Utility for triggering explosions with explicit control over terrain damage.
 */
object ExplosionUtils {

    /**
     * Triggers an explosion at [position]. [affectsBlocks] controls whether terrain is
     * destroyed — false gives a purely visual/knockback explosion safe for arenas or effects.
     */
    fun explode(level: ServerLevel, position: Vec3, power: Float, affectsBlocks: Boolean = false) {
        val interaction = if (affectsBlocks) Level.ExplosionInteraction.BLOCK else Level.ExplosionInteraction.NONE
        level.explode(null, position.x, position.y, position.z, power, interaction)
    }
}
