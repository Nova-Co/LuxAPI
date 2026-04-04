package com.novaco.luxapi.commons.data

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CooldownManagerTest {

    private lateinit var cooldownManager: CooldownManager<UUID>
    private val testPlayerId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        cooldownManager = CooldownManager()
    }

    @Test
    fun `test set cooldown and check active state`() {
        // Set cooldown for 5 seconds
        cooldownManager.setCooldown(testPlayerId, 5000L)

        // It should be on cooldown immediately
        assertTrue(cooldownManager.isOnCooldown(testPlayerId), "Player should be on cooldown")
        assertTrue(cooldownManager.getRemainingTime(testPlayerId) > 0, "Remaining time should be greater than 0")
    }

    @Test
    fun `test cooldown expiration`() {
        // Set a very short cooldown (20 milliseconds)
        cooldownManager.setCooldown(testPlayerId, 20L)

        // Wait for 30 milliseconds to ensure it expires
        Thread.sleep(30)

        // It should no longer be on cooldown
        assertFalse(cooldownManager.isOnCooldown(testPlayerId), "Cooldown should have expired")
        assertEquals(0L, cooldownManager.getRemainingTime(testPlayerId), "Remaining time should be exactly 0")
    }

    @Test
    fun `test manual cooldown clearance`() {
        cooldownManager.setCooldown(testPlayerId, 10000L)
        assertTrue(cooldownManager.isOnCooldown(testPlayerId))

        // Manually clear it
        cooldownManager.clearCooldown(testPlayerId)

        assertFalse(cooldownManager.isOnCooldown(testPlayerId), "Cooldown should be cleared manually")
    }
}