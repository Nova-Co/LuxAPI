package com.novaco.luxapi.core.region

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

/**
 * Utility for working with cuboid regions and safe-spot lookups on a [ServerLevel].
 */
object RegionUtils {

    /**
     * Iterates every block position in the axis-aligned cuboid between [from] and [to] (inclusive).
     * Corner order doesn't matter.
     */
    fun blocksBetween(from: BlockPos, to: BlockPos): Iterable<BlockPos> {
        return BlockPos.betweenClosed(from, to)
    }

    /**
     * Checks whether [position] is within [radius] blocks of [center] (straight-line distance).
     */
    fun isWithinRadius(center: Vec3, position: Vec3, radius: Double): Boolean {
        return center.distanceToSqr(position) <= radius * radius
    }

    /**
     * Finds the first position at or above [start] that has two air blocks with solid ground
     * beneath, scanning upward. Returns null if none is found within [maxHeight] blocks.
     */
    fun findSafeSpot(level: ServerLevel, start: BlockPos, maxHeight: Int = 16): BlockPos? {
        var pos = start
        repeat(maxHeight) {
            val below = pos.below()
            val groundSolid = !level.isEmptyBlock(below)
            val feetClear = level.isEmptyBlock(pos)
            val headClear = level.isEmptyBlock(pos.above())
            if (groundSolid && feetClear && headClear) return pos
            pos = pos.above()
        }
        return null
    }
}
