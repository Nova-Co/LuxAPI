package com.novaco.luxapi.commons.leaderboard

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class LeaderboardTest {

    @Test
    fun `test descending order ranks highest score first`() {
        val board = Leaderboard("test")
        val alice = UUID.randomUUID()
        val bob = UUID.randomUUID()

        board.setScore(alice, "Alice", 50.0)
        board.setScore(bob, "Bob", 100.0)

        val top = board.getTop(10)

        assertEquals("Bob", top[0].name)
        assertEquals("Alice", top[1].name)
    }

    @Test
    fun `test ascending order ranks lowest score first`() {
        val board = Leaderboard("speedrun", ascending = true)
        val alice = UUID.randomUUID()
        val bob = UUID.randomUUID()

        board.setScore(alice, "Alice", 50.0)
        board.setScore(bob, "Bob", 100.0)

        val top = board.getTop(10)

        assertEquals("Alice", top[0].name)
        assertEquals("Bob", top[1].name)
    }

    @Test
    fun `test addScore accumulates from zero for new entries`() {
        val board = Leaderboard("kills")
        val player = UUID.randomUUID()

        board.addScore(player, "Player", 3.0)
        board.addScore(player, "Player", 2.0)

        assertEquals(5.0, board.getScore(player))
    }

    @Test
    fun `test getTop respects the requested limit`() {
        val board = Leaderboard("balance")
        repeat(5) { board.setScore(UUID.randomUUID(), "P$it", it.toDouble()) }

        assertEquals(2, board.getTop(2).size)
    }

    @Test
    fun `test getRank returns 1-based position or null when absent`() {
        val board = Leaderboard("balance")
        val alice = UUID.randomUUID()
        val bob = UUID.randomUUID()
        val stranger = UUID.randomUUID()

        board.setScore(alice, "Alice", 100.0)
        board.setScore(bob, "Bob", 50.0)

        assertEquals(1, board.getRank(alice))
        assertEquals(2, board.getRank(bob))
        assertNull(board.getRank(stranger))
    }

    @Test
    fun `test removeEntry and clear drop tracked players`() {
        val board = Leaderboard("balance")
        val alice = UUID.randomUUID()
        val bob = UUID.randomUUID()

        board.setScore(alice, "Alice", 10.0)
        board.setScore(bob, "Bob", 20.0)

        board.removeEntry(alice)
        assertNull(board.getScore(alice))
        assertEquals(1, board.size())

        board.clear()
        assertEquals(0, board.size())
    }
}
