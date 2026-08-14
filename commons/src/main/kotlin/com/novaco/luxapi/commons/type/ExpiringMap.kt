package com.novaco.luxapi.commons.type

import java.util.concurrent.ConcurrentHashMap

/**
 * A thread-safe map of keys to a future expiry timestamp. This is the shared
 * "is this still active, and if not, drop it" primitive behind
 * [com.novaco.luxapi.commons.data.CooldownManager] and
 * [com.novaco.luxapi.commons.data.TimeGateManager] — both used to hand-roll the
 * same expiry check separately.
 *
 * @param K The type of key being tracked (e.g. a player UUID).
 */
class ExpiringMap<K> {

    private val entries = ConcurrentHashMap<K, Long>()

    /**
     * Marks [key] as active for [durationMillis] from now, replacing any existing entry.
     */
    fun set(key: K, durationMillis: Long) {
        entries[key] = System.currentTimeMillis() + durationMillis
    }

    /**
     * Returns true if [key] has an entry that hasn't expired yet.
     * Removes the entry as a side effect if it's found to be expired.
     */
    fun isActive(key: K): Boolean {
        val expiryTime = entries[key] ?: return false
        if (System.currentTimeMillis() >= expiryTime) {
            entries.remove(key)
            return false
        }
        return true
    }

    /**
     * Returns the time in milliseconds until [key]'s entry expires, or 0 if it
     * has no entry or has already expired. Removes the entry as a side effect
     * if it's found to be expired.
     */
    fun remaining(key: K): Long {
        val expiryTime = entries[key] ?: return 0L
        val remaining = expiryTime - System.currentTimeMillis()
        if (remaining <= 0) {
            entries.remove(key)
            return 0L
        }
        return remaining
    }

    /**
     * Removes [key]'s entry immediately, regardless of whether it has expired.
     */
    fun clear(key: K) {
        entries.remove(key)
    }

    /**
     * Removes every entry that has already expired. Call this periodically
     * (e.g. from a scheduler) to bound memory use for keys that are never
     * re-checked via [isActive]/[remaining] after they expire.
     */
    fun cleanUp() {
        val currentTime = System.currentTimeMillis()
        entries.entries.removeIf { currentTime >= it.value }
    }
}
