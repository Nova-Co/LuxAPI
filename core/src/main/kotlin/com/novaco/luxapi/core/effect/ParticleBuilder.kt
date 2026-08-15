package com.novaco.luxapi.core.effect

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A fluent builder for configuring and spawning particle effects.
 * Simplifies the complex native particle spawning methods into a readable chain.
 */
class ParticleBuilder(private val particle: ParticleOptions) {

    private var count: Int = 1
    private var offsetX: Double = 0.0
    private var offsetY: Double = 0.0
    private var offsetZ: Double = 0.0
    private var speed: Double = 0.0

    /**
     * Sets the amount of particles to spawn.
     *
     * @param amount The number of particles.
     */
    fun count(amount: Int): ParticleBuilder {
        this.count = amount
        return this
    }

    /**
     * Sets the spread or offset of the particles.
     * A larger offset means the particles will scatter over a wider area.
     *
     * @param x The spread on the X axis.
     * @param y The spread on the Y axis.
     * @param z The spread on the Z axis.
     */
    fun offset(x: Double, y: Double, z: Double): ParticleBuilder {
        this.offsetX = x
        this.offsetY = y
        this.offsetZ = z
        return this
    }

    /**
     * Sets the speed of the particles.
     * Some particles (like explosions) use this for size, while others use it for velocity.
     *
     * @param value The speed multiplier.
     */
    fun speed(value: Double): ParticleBuilder {
        this.speed = value
        return this
    }

    /**
     * Spawns the configured particles at the specified location in the world.
     *
     * @param level The server level where the particles will appear.
     * @param x The absolute X coordinate.
     * @param y The absolute Y coordinate.
     * @param z The absolute Z coordinate.
     */
    fun spawn(level: ServerLevel, x: Double, y: Double, z: Double) {
        level.sendParticles(
            particle,
            x, y, z,
            count,
            offsetX, offsetY, offsetZ,
            speed
        )
    }

    /**
     * Spawns the configured particles at the specified Vector location.
     *
     * @param level The server level where the particles will appear.
     * @param position The exact 3D vector position.
     */
    fun spawn(level: ServerLevel, position: Vec3) {
        spawn(level, position.x, position.y, position.z)
    }

    /**
     * Spawns the configured particles once at each of the given points.
     * Pairs with the shape helpers in the companion object (line, circle, sphere, helix).
     */
    fun spawnAlong(level: ServerLevel, points: List<Vec3>) {
        points.forEach { spawn(level, it) }
    }

    companion object {

        /**
         * Generates evenly spaced points along a straight line between two positions.
         */
        fun line(start: Vec3, end: Vec3, points: Int): List<Vec3> {
            if (points < 2) return listOf(start)
            return (0 until points).map { i ->
                val t = i.toDouble() / (points - 1)
                start.lerp(end, t)
            }
        }

        /**
         * Generates points around a horizontal circle centered on [center].
         */
        fun circle(center: Vec3, radius: Double, points: Int): List<Vec3> {
            if (points < 1) return emptyList()
            return (0 until points).map { i ->
                val angle = 2.0 * PI * i / points
                center.add(cos(angle) * radius, 0.0, sin(angle) * radius)
            }
        }

        /**
         * Generates roughly evenly-distributed points across the surface of a sphere
         * using a Fibonacci sphere distribution.
         */
        fun sphere(center: Vec3, radius: Double, points: Int): List<Vec3> {
            if (points < 1) return emptyList()
            val goldenAngle = PI * (3.0 - sqrt(5.0))
            return (0 until points).map { i ->
                val y = 1.0 - (i.toDouble() / (points - 1).coerceAtLeast(1)) * 2.0
                val radiusAtY = sqrt((1.0 - y * y).coerceAtLeast(0.0))
                val theta = goldenAngle * i
                center.add(cos(theta) * radiusAtY * radius, y * radius, sin(theta) * radiusAtY * radius)
            }
        }

        /**
         * Generates points along a vertical spiral (helix) rising from [center].
         */
        fun helix(center: Vec3, radius: Double, height: Double, turns: Double, points: Int): List<Vec3> {
            if (points < 1) return emptyList()
            return (0 until points).map { i ->
                val t = i.toDouble() / points
                val angle = 2.0 * PI * turns * t
                center.add(cos(angle) * radius, height * t, sin(angle) * radius)
            }
        }
    }
}