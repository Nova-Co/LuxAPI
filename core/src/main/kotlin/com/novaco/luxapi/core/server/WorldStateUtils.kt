package com.novaco.luxapi.core.server

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Difficulty

/**
 * Utility for reading/setting a level's time and weather, and the server's difficulty.
 */
object WorldStateUtils {

    fun getTime(level: ServerLevel): Long = level.dayTime

    fun setTime(level: ServerLevel, ticks: Long) {
        level.dayTime = ticks
    }

    /**
     * Sets weather state directly. [clearDuration]/[weatherDuration] are in ticks;
     * [isRaining]/[isThundering] set the active state for that duration.
     */
    fun setWeather(level: ServerLevel, clearDuration: Int, weatherDuration: Int, isRaining: Boolean, isThundering: Boolean) {
        level.setWeatherParameters(clearDuration, weatherDuration, isRaining, isThundering)
    }

    fun setDifficulty(server: MinecraftServer, difficulty: Difficulty, locked: Boolean = false) {
        server.setDifficulty(difficulty, locked)
    }
}
