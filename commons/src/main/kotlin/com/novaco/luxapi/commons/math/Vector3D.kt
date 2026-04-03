package com.novaco.luxapi.commons.math

import kotlin.math.sqrt

/**
 * Represents an immutable 3D coordinate in space.
 * Ideal for cross-platform location mapping where native Minecraft classes cannot be used.
 *
 * @property x The X coordinate.
 * @property y The Y coordinate.
 * @property z The Z coordinate.
 */
data class Vector3D(
    val x: Double,
    val y: Double,
    val z: Double
) {

    /**
     * Calculates the exact distance between this vector and another.
     * Note: This uses a square root operation, which can be computationally heavy if called
     * thousands of times per tick. Use [distanceSquared] for simple comparisons.
     *
     * @param other The target vector.
     * @return The exact distance.
     */
    fun distance(other: Vector3D): Double {
        return sqrt(distanceSquared(other))
    }

    /**
     * Calculates the squared distance between this vector and another.
     * Highly recommended for radius checks (e.g., checking if distanceSquared < radius * radius)
     * to save CPU cycles.
     *
     * @param other The target vector.
     * @return The squared distance.
     */
    fun distanceSquared(other: Vector3D): Double {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        return (dx * dx) + (dy * dy) + (dz * dz)
    }

    /**
     * Adds another vector to this one, returning a new Vector3D instance.
     */
    fun add(other: Vector3D): Vector3D {
        return Vector3D(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    /**
     * Subtracts another vector from this one, returning a new Vector3D instance.
     */
    fun subtract(other: Vector3D): Vector3D {
        return Vector3D(this.x - other.x, this.y - other.y, this.z - other.z)
    }

    /**
     * Multiplies this vector by a scalar value, returning a new Vector3D instance.
     */
    fun multiply(scalar: Double): Vector3D {
        return Vector3D(this.x * scalar, this.y * scalar, this.z * scalar)
    }
}