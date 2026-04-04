package com.novaco.luxapi.core.effect

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ParticleBuilderTest {

    @Test
    fun `test particle builder configuration and coordinate spawning`() {
        // 1. Create a fake server level and a fake particle type
        val mockLevel = mock(ServerLevel::class.java)
        val mockParticle = mock(ParticleOptions::class.java)

        // 2. Configure our builder
        val builder = ParticleBuilder(mockParticle)
            .count(50)
            .offset(0.5, 1.0, 0.5)
            .speed(0.1)

        // 3. Trigger the spawn method
        builder.spawn(mockLevel, 100.0, 64.0, -100.0)

        // 4. Verify the native Minecraft method was triggered with our exact builder values
        verify(mockLevel).sendParticles(
            mockParticle,    // particle type
            100.0, 64.0, -100.0, // x, y, z
            50,              // count
            0.5, 1.0, 0.5,   // offsetX, offsetY, offsetZ
            0.1              // speed
        )
    }

    @Test
    fun `test particle builder vec3 spawning delegation`() {
        val mockLevel = mock(ServerLevel::class.java)
        val mockParticle = mock(ParticleOptions::class.java)

        val position = Vec3(10.0, 20.0, 30.0)

        val builder = ParticleBuilder(mockParticle).count(1)

        // Trigger the Vec3 overload
        builder.spawn(mockLevel, position)

        // Verify it successfully extracted the X, Y, Z from the vector
        verify(mockLevel).sendParticles(
            mockParticle,
            10.0, 20.0, 30.0,
            1,
            0.0, 0.0, 0.0,
            0.0
        )
    }
}