package com.novaco.luxapi.commons.data

import java.util.concurrent.ConcurrentHashMap

/**
 * A generic, thread-safe manager for handling cooldowns.
 * Can be used for players (UUID), IP addresses (String), or any other key type.
 *
 * @param T The type of the key used to track the cooldown (e.g., java.util.UUID).
 */
class CooldownManager<T> {

    private val cooldowns = ConcurrentHashMap<T, Long>()

    /**
     * Sets a cooldown for the specified key.
     *
     * @param key The identifier to apply the cooldown to.
     * @param durationMillis The duration of the cooldown in milliseconds.
     */
    fun setCooldown(key: T, durationMillis: Long) {
        cooldowns[key] = System.currentTimeMillis() + durationMillis
    }

    /**
     * Checks if the specified key is currently on cooldown.
     *
     * @param key The identifier to check.
     * @return True if the cooldown is still active, false otherwise.
     */
    fun isOnCooldown(key: T): Boolean {
        val expiryTime = cooldowns[key] ?: return false
        if (System.currentTimeMillis() >= expiryTime) {
            cooldowns.remove(key)
            return false
        }
        return true
    }

    /**
     * Calculates the remaining cooldown time for the specified key.
     *
     * @param key The identifier to check.
     * @return The remaining time in milliseconds, or 0 if the cooldown has expired.
     */
    fun getRemainingTime(key: T): Long {
        val expiryTime = cooldowns[key] ?: return 0L
        val remaining = expiryTime - System.currentTimeMillis()

        if (remaining <= 0) {
            cooldowns.remove(key)
            return 0L
        }
        return remaining
    }

    /**
     * Clears the cooldown for a specific key manually.
     *
     * @param key The identifier to clear.
     */
    fun clearCooldown(key: T) {
        cooldowns.remove(key)
    }
}