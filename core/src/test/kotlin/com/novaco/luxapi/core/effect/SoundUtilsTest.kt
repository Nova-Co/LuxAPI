package com.novaco.luxapi.core.effect

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class SoundUtilsTest {

    @Test
    fun `test playFor plays sound privately to the player`() {
        // 1. Mock our engine objects
        val mockPlayer = mock(ServerPlayer::class.java)
        val mockSound = mock(SoundEvent::class.java)

        // 2. Execute our utility method
        SoundUtils.playFor(
            player = mockPlayer,
            sound = mockSound,
            category = SoundSource.PLAYERS,
            volume = 0.5f,
            pitch = 1.2f
        )

        // 3. Verify it routed ONLY to the player's notify method
        verify(mockPlayer).playNotifySound(mockSound, SoundSource.PLAYERS, 0.5f, 1.2f)
    }

    @Test
    fun `test playAt broadcasts sound to the world level`() {
        // 1. Mock our engine objects
        val mockLevel = mock(ServerLevel::class.java)
        val mockSound = mock(SoundEvent::class.java)
        val position = Vec3(50.0, 100.0, 50.0)

        // 2. Execute our utility method
        SoundUtils.playAt(
            level = mockLevel,
            position = position,
            sound = mockSound,
            category = SoundSource.BLOCKS,
            volume = 1.0f,
            pitch = 0.8f
        )

        // 3. Verify it routed to the level's global play method.
        // Note: Passing 'null' as the player argument in level.playSound means EVERYONE hears it.
        verify(mockLevel).playSound(
            null,
            50.0, 100.0, 50.0,
            mockSound,
            SoundSource.BLOCKS,
            1.0f,
            0.8f
        )
    }
}