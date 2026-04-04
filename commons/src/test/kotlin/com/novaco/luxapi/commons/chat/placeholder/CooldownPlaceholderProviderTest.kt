package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.data.TimeGateManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CooldownPlaceholderProviderTest {

    private lateinit var timeGate: TimeGateManager<UUID>
    private lateinit var provider: CooldownPlaceholderProvider
    private lateinit var dummyPlayer: DummyPlaceholderPlayer

    @BeforeEach
    fun setup() {
        timeGate = TimeGateManager()
        provider = CooldownPlaceholderProvider(timeGate)
        dummyPlayer = DummyPlaceholderPlayer(UUID.randomUUID(), "CooldownTester")
    }

    @Test
    fun `test cooldown placeholder formatting`() {
        assertEquals("cooldown", provider.identifier(), "Identifier must strictly be 'cooldown'.")

        // No cooldown active (Should output "Ready" based on TimeFormatUtils)
        val readyResult = provider.onPlaceholderRequest(dummyPlayer, "daily_kit")
        assertEquals("Ready", readyResult, "Inactive cooldowns should format as 'Ready'.")

        // Apply a 2-minute cooldown (120,000 milliseconds)
        timeGate.setCooldown("daily_kit", dummyPlayer.uniqueId, 120000L)

        val activeResult = provider.onPlaceholderRequest(dummyPlayer, "daily_kit")
        // Note: Depending on processing time, it might be 1m 59s.
        // We verify it contains "1m" or "2m" to account for execution delay.
        assertNotNull(activeResult)
        assertTrue(activeResult!!.contains("1m") || activeResult.contains("2m"), "Should output formatted time string.")
    }

    @Test
    fun `test cooldown placeholder with null player`() {
        val nullResult = provider.onPlaceholderRequest(null, "daily_kit")
        assertNull(nullResult, "A null player context should return null for cooldown requests.")
    }
}