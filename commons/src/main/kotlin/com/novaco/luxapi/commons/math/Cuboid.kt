package com.novaco.luxapi.commons.math

import kotlin.math.max
import kotlin.math.min

/**
 * Represents a 3D Axis-Aligned Bounding Box (AABB) defined by two spatial points.
 * Automatically resolves the minimum and maximum boundaries regardless of the order
 * the points are provided. Perfect for defining arenas, safe zones, and trigger areas.
 */
class Cuboid(point1: Vector3D, point2: Vector3D) {

    val minX: Double = min(point1.x, point2.x)
    val minY: Double = min(point1.y, point2.y)
    val minZ: Double = min(point1.z, point2.z)

    val maxX: Double = max(point1.x, point2.x)
    val maxY: Double = max(point1.y, point2.y)
    val maxZ: Double = max(point1.z, point2.z)

    /**
     * Checks if a specific Vector3D is physically inside this cuboid.
     *
     * @param vector The location to check.
     * @return True if the vector is within the boundaries, false otherwise.
     */
    fun contains(vector: Vector3D): Boolean {
        return contains(vector.x, vector.y, vector.z)
    }

    /**
     * Checks if raw coordinates are physically inside this cuboid.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param z The Z coordinate.
     * @return True if the coordinates are within the boundaries, false otherwise.
     */
    fun contains(x: Double, y: Double, z: Double): Boolean {
        return x in minX..maxX &&
                y in minY..maxY &&
                z in minZ..maxZ
    }

    /**
     * Calculates the exact center point of this cuboid.
     * Useful for teleporting players to the middle of an arena.
     *
     * @return A Vector3D representing the center.
     */
    fun getCenter(): Vector3D {
        return Vector3D(
            minX + (maxX - minX) / 2.0,
            minY + (maxY - minY) / 2.0,
            minZ + (maxZ - minZ) / 2.0
        )
    }

    /**
     * Calculates the total volumetric area of this cuboid.
     *
     * @return The volume (width * height * depth).
     */
    fun getVolume(): Double {
        val width = maxX - minX
        val height = maxY - minY
        val depth = maxZ - minZ
        return width * height * depth
    }
}