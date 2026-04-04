package com.novaco.luxapi.core.effect

import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

/**
 * A utility class for playing sound effects in the world or privately to specific players.
 */
object SoundUtils {

    /**
     * Plays a sound privately to a specific player.
     * Other players nearby will not hear this sound.
     *
     * @param player The player who will hear the sound.
     * @param sound The sound event to play.
     * @param category The audio category (e.g., MASTER, PLAYERS, BLOCKS).
     * @param volume The volume multiplier (default is 1.0).
     * @param pitch The pitch modifier (default is 1.0, lower is deeper, higher is squeakier).
     */
    fun playFor(
        player: ServerPlayer,
        sound: SoundEvent,
        category: SoundSource = SoundSource.MASTER,
        volume: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        player.playNotifySound(sound, category, volume, pitch)
    }

    /**
     * Broadcasts a sound at a specific location in the world.
     * Anyone within the sound's range will hear it.
     *
     * @param level The server level where the sound originates.
     * @param position The 3D location of the sound.
     * @param sound The sound event to play.
     * @param category The audio category.
     * @param volume The volume multiplier.
     * @param pitch The pitch modifier.
     */
    fun playAt(
        level: ServerLevel,
        position: Vec3,
        sound: SoundEvent,
        category: SoundSource = SoundSource.MASTER,
        volume: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        level.playSound(
            null,
            position.x,
            position.y,
            position.z,
            sound,
            category,
            volume,
            pitch
        )
    }
}