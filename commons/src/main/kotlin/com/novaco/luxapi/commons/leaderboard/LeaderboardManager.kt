package com.novaco.luxapi.commons.leaderboard

import java.util.concurrent.ConcurrentHashMap

/**
 * Central registry of named [Leaderboard] instances, so multiple systems (economy,
 * boss DPS, playtime, ...) can each own a leaderboard by id without colliding.
 */
object LeaderboardManager {

    private val leaderboards = ConcurrentHashMap<String, Leaderboard>()

    /**
     * Retrieves an existing leaderboard by id, or creates one if it doesn't exist yet.
     */
    fun getOrCreate(id: String, ascending: Boolean = false): Leaderboard {
        return leaderboards.computeIfAbsent(id) { Leaderboard(id, ascending) }
    }

    /**
     * Retrieves an existing leaderboard by id, or null if none has been created.
     */
    fun get(id: String): Leaderboard? = leaderboards[id]

    /**
     * Removes a leaderboard entirely, freeing its entries.
     */
    fun remove(id: String) {
        leaderboards.remove(id)
    }

    /**
     * Returns the ids of every currently registered leaderboard.
     */
    fun getIds(): Set<String> = leaderboards.keys
}
