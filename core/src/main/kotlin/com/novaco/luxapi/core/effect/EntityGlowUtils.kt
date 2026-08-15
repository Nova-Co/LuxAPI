package com.novaco.luxapi.core.effect

import net.minecraft.world.entity.Entity

/**
 * Direct per-entity glow outline, independent of scoreboard teams. Use [TeamUtils][com.novaco.luxapi.core.scoreboard.TeamUtils]
 * instead when a specific outline color (rather than the default white) is needed.
 */
object EntityGlowUtils {

    fun setGlowing(entity: Entity, glowing: Boolean = true) {
        entity.setGlowingTag(glowing)
    }

    fun isGlowing(entity: Entity): Boolean {
        return entity.isCurrentlyGlowing
    }
}
