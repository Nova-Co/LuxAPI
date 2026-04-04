package com.novaco.luxapi.core.effect

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

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
}