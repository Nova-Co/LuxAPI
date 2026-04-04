package com.novaco.luxapi.commons.data

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class TimeGateManagerTest {

    private lateinit var timeGate: TimeGateManager<UUID>
    private val playerOne = UUID.randomUUID()
    private val playerTwo = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        timeGate = TimeGateManager()
    }

    @Test
    fun `test categories do not overlap`() {
        timeGate.setCooldown("heal", playerOne, 5000L)

        // playerOne should be on cooldown for 'heal' but NOT for 'kit'
        assertTrue(timeGate.isOnCooldown("heal", playerOne), "Should be on heal cooldown")
        assertFalse(timeGate.isOnCooldown("kit", playerOne), "Should NOT be on kit cooldown")

        // playerTwo should not be on any cooldown
        assertFalse(timeGate.isOnCooldown("heal", playerTwo), "Player Two should not share Player One's cooldown")
    }

    @Test
    fun `test cleanUp removes expired entries to save memory`() {
        // Set a 10ms cooldown
        timeGate.setCooldown("daily", playerOne, 10L)

        // Wait for expiration
        Thread.sleep(20)

        // Trigger memory cleanup
        timeGate.cleanUp()

        // Cooldown should be gone and safely return 0
        assertEquals(0L, timeGate.getRemainingTime("daily", playerOne))
    }
}