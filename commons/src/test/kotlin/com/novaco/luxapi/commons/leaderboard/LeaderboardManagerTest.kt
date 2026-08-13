package com.novaco.luxapi.commons.leaderboard

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class LeaderboardManagerTest {

    @AfterEach
    fun tearDown() {
        LeaderboardManager.getIds().toList().forEach { LeaderboardManager.remove(it) }
    }

    @Test
    fun `test getOrCreate returns the same instance for the same id`() {
        val first = LeaderboardManager.getOrCreate("kills")
        val second = LeaderboardManager.getOrCreate("kills")

        assertSame(first, second)
    }

    @Test
    fun `test get returns null for an unregistered id`() {
        assertNull(LeaderboardManager.get("nonexistent"))
    }

    @Test
    fun `test remove drops the leaderboard from the registry`() {
        LeaderboardManager.getOrCreate("playtime")
        assertNotNull(LeaderboardManager.get("playtime"))

        LeaderboardManager.remove("playtime")

        assertNull(LeaderboardManager.get("playtime"))
    }

    @Test
    fun `test getIds reflects all currently registered leaderboards`() {
        LeaderboardManager.getOrCreate("balance")
        LeaderboardManager.getOrCreate("kills")

        val ids = LeaderboardManager.getIds()

        assertTrue(ids.containsAll(listOf("balance", "kills")))
    }
}
