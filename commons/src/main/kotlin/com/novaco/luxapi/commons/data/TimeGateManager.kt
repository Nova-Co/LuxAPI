package com.novaco.luxapi.commons.data

import java.util.concurrent.ConcurrentHashMap

/**
 * A global, category-based manager for handling time gates and cooldowns.
 * Designed to support multiple cooldown types (e.g., "heal", "daily_reward") concurrently.
 *
 * @param T The type of the key used to track the cooldown (e.g., java.util.UUID).
 */
class TimeGateManager<T> {

    private val gates = ConcurrentHashMap<String, ConcurrentHashMap<T, Long>>()

    /**
     * Retrieves or creates a concurrent map for a specific cooldown category.
     */
    private fun getCategoryMap(category: String): ConcurrentHashMap<T, Long> {
        return gates.getOrPut(category) { ConcurrentHashMap() }
    }

    /**
     * Sets a cooldown for the specified key within a category.
     *
     * @param category The identifier for the type of cooldown (e.g., "kit_starter").
     * @param key The identifier to apply the cooldown to (e.g., Player UUID).
     * @param durationMillis The duration of the cooldown in milliseconds.
     */
    fun setCooldown(category: String, key: T, durationMillis: Long) {
        getCategoryMap(category)[key] = System.currentTimeMillis() + durationMillis
    }

    /**
     * Checks if the specified key is currently on cooldown for a given category.
     * Automatically cleans up expired entries upon checking.
     *
     * @param category The cooldown category.
     * @param key The identifier to check.
     * @return True if the cooldown is still active, false otherwise.
     */
    fun isOnCooldown(category: String, key: T): Boolean {
        val categoryMap = getCategoryMap(category)
        val expiryTime = categoryMap[key] ?: return false

        if (System.currentTimeMillis() >= expiryTime) {
            categoryMap.remove(key)
            return false
        }
        return true
    }

    /**
     * Calculates the remaining cooldown time for the specified key.
     *
     * @param category The cooldown category.
     * @param key The identifier to check.
     * @return The remaining time in milliseconds, or 0 if the cooldown has expired.
     */
    fun getRemainingTime(category: String, key: T): Long {
        val categoryMap = getCategoryMap(category)
        val expiryTime = categoryMap[key] ?: return 0L
        val remaining = expiryTime - System.currentTimeMillis()

        if (remaining <= 0) {
            categoryMap.remove(key)
            return 0L
        }
        return remaining
    }

    /**
     * Clears the cooldown for a specific key manually.
     *
     * @param category The cooldown category.
     * @param key The identifier to clear.
     */
    fun clearCooldown(category: String, key: T) {
        gates[category]?.remove(key)
    }

    /**
     * Cleans up all expired cooldowns from memory to prevent memory leaks over time.
     * Recommended to be called periodically via a scheduler.
     */
    fun cleanUp() {
        val currentTime = System.currentTimeMillis()
        gates.values.forEach { categoryMap ->
            categoryMap.entries.removeIf { currentTime >= it.value }
        }
    }
}