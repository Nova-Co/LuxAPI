package com.novaco.luxapi.cobblemon.progression

import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.UUID

/**
 * An in-memory tracker for consecutive Pokémon captures (Catch Streaks).
 * This system is highly useful for implementing Shiny Hunting mechanics,
 * where catching the same species repeatedly increases shiny odds or guarantees perfect IVs.
 */
object CatchStreakManager {

    /**
     * Internal map storing the player's UUID, their current streak target (species name),
     * and the consecutive catch count.
     */
    private val activeStreaks: MutableMap<UUID, Pair<String, Int>> = mutableMapOf()

    /**
     * Registers a new capture event for the player and updates their streak.
     * If the player catches a different species, the streak resets to 1.
     *
     * @param player The player who caught the Pokémon.
     * @param caughtSpecies The name of the captured species.
     * @return The current streak count after this capture.
     */
    fun registerCatch(player: LuxPlayer, caughtSpecies: String): Int {
        val uuid = player.uniqueId
        val species = caughtSpecies.lowercase()

        val currentStreak = activeStreaks[uuid]

        if (currentStreak != null && currentStreak.first == species) {
            val newCount = currentStreak.second + 1
            activeStreaks[uuid] = Pair(species, newCount)
            return newCount
        } else {
            activeStreaks[uuid] = Pair(species, 1)
            return 1
        }
    }

    /**
     * Retrieves the player's current ongoing catch streak count.
     *
     * @param player The target player.
     * @return The current consecutive catch count. Returns 0 if no streak exists.
     */
    fun getCurrentStreakCount(player: LuxPlayer): Int {
        return activeStreaks[player.uniqueId]?.second ?: 0
    }

    /**
     * Retrieves the name of the Pokémon species the player is currently streaking.
     *
     * @param player The target player.
     * @return The species name, or null if there is no active streak.
     */
    fun getCurrentStreakSpecies(player: LuxPlayer): String? {
        return activeStreaks[player.uniqueId]?.first
    }

    /**
     * Manually resets a player's catch streak (e.g., when they disconnect or fail a quest).
     *
     * @param player The target player.
     */
    fun resetStreak(player: LuxPlayer) {
        activeStreaks.remove(player.uniqueId)
    }
}