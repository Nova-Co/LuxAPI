package com.novaco.luxapi.commons.leaderboard

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * A generic, in-memory ranking table — top balance/kills/playtime, or any other
 * numeric stat a dev wants to rank players by. Cross-platform by design: it only
 * ever deals in UUID/name/score, never a platform player type.
 *
 * @param id The unique identifier of this leaderboard (e.g. "balance", "kills").
 * @param ascending When true, lower scores rank higher (e.g. fastest-time leaderboards).
 */
class Leaderboard(val id: String, private val ascending: Boolean = false) {

    private val scores = ConcurrentHashMap<UUID, LeaderboardEntry>()

    /**
     * Sets a player's absolute score, replacing any prior value.
     */
    fun setScore(uuid: UUID, name: String, score: Double) {
        scores[uuid] = LeaderboardEntry(uuid, name, score)
    }

    /**
     * Adds (or subtracts, via a negative delta) to a player's current score.
     * Players with no existing entry start from zero.
     */
    fun addScore(uuid: UUID, name: String, delta: Double) {
        val current = scores[uuid]?.score ?: 0.0
        setScore(uuid, name, current + delta)
    }

    /**
     * Removes a player from this leaderboard entirely.
     */
    fun removeEntry(uuid: UUID) {
        scores.remove(uuid)
    }

    /**
     * Retrieves a player's current score, or null if they have no entry.
     */
    fun getScore(uuid: UUID): Double? = scores[uuid]?.score

    /**
     * Returns the top [limit] entries, ordered by rank (best first).
     */
    fun getTop(limit: Int): List<LeaderboardEntry> {
        val comparator = if (ascending) {
            compareBy<LeaderboardEntry> { it.score }
        } else {
            compareByDescending { it.score }
        }
        return scores.values.sortedWith(comparator).take(limit)
    }

    /**
     * Returns a player's 1-based rank on this leaderboard, or null if they have no entry.
     */
    fun getRank(uuid: UUID): Int? {
        if (!scores.containsKey(uuid)) return null
        val index = getTop(scores.size).indexOfFirst { it.uuid == uuid }
        return if (index == -1) null else index + 1
    }

    /**
     * The total number of ranked entries.
     */
    fun size(): Int = scores.size

    /**
     * Clears every entry from this leaderboard.
     */
    fun clear() {
        scores.clear()
    }
}
