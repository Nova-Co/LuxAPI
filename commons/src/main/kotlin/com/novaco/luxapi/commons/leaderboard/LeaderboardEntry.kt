package com.novaco.luxapi.commons.leaderboard

import java.util.UUID

/**
 * A single ranked entry within a [Leaderboard] — a player's UUID/name paired with their score.
 */
data class LeaderboardEntry(
    val uuid: UUID,
    val name: String,
    val score: Double
)
