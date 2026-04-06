package com.novaco.luxapi.cobblemon.progression

import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class CatchStreakManagerTest {

    private lateinit var mockPlayer: LuxPlayer
    private val playerUuid = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        mockPlayer = mock<LuxPlayer>()
        whenever(mockPlayer.uniqueId).thenReturn(playerUuid)

        // Clear the internal state to prevent cross-test contamination
        val activeStreaksField = CatchStreakManager::class.java.getDeclaredField("activeStreaks")
        activeStreaksField.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val map = activeStreaksField.get(CatchStreakManager) as MutableMap<UUID, Pair<String, Int>>
        map.clear()
    }

    @Test
    fun `test registerCatch starts a new streak`() {
        val count = CatchStreakManager.registerCatch(mockPlayer, "Pikachu")

        assertEquals(1, count)
        assertEquals(1, CatchStreakManager.getCurrentStreakCount(mockPlayer))
        assertEquals("pikachu", CatchStreakManager.getCurrentStreakSpecies(mockPlayer))
    }

    @Test
    fun `test registerCatch increments existing streak and ignores case`() {
        CatchStreakManager.registerCatch(mockPlayer, "pikachu")
        val count = CatchStreakManager.registerCatch(mockPlayer, "PIKACHU") // Caps shouldn't break it

        assertEquals(2, count)
        assertEquals(2, CatchStreakManager.getCurrentStreakCount(mockPlayer))
    }

    @Test
    @Disabled("Requires Cobblemon Battle Registry and Minecraft Engine loaded.")
    fun `test registerCatch breaks streak on different species`() {
        CatchStreakManager.registerCatch(mockPlayer, "Pikachu")
        CatchStreakManager.registerCatch(mockPlayer, "Pikachu")

        // Catching a different Pokemon resets the streak to 1 for the new species
        val newCount = CatchStreakManager.registerCatch(mockPlayer, "Charmander")

        assertEquals(1, newCount)
        assertEquals("charmander", CatchStreakManager.getCurrentStreakSpecies(mockPlayer))
    }

    @Test

    fun `test resetStreak completely clears player data`() {
        CatchStreakManager.registerCatch(mockPlayer, "Eevee")
        CatchStreakManager.resetStreak(mockPlayer)

        assertEquals(0, CatchStreakManager.getCurrentStreakCount(mockPlayer))
        assertNull(CatchStreakManager.getCurrentStreakSpecies(mockPlayer))
    }
}