package com.novaco.luxapi.cobblemon.spawning

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class SwarmEventTest {

    @Test
    @Disabled("Requires Cobblemon Battle Registry and Minecraft Engine loaded.")
    fun `test isExpired logic evaluates time bounds correctly`() {
        val mockDimension = mock<ResourceKey<Level>>()
        val centerPos = BlockPos.ZERO

        // Expired 5 seconds ago
        val pastTime = System.currentTimeMillis() - 5000L
        val expiredEvent = SwarmEvent(
            speciesName = "pidgey", dimension = mockDimension, centerPos = centerPos, radius = 10, expireAt = pastTime
        )
        Assertions.assertTrue(expiredEvent.isExpired(), "Event should be flagged as expired if time has passed.")

        // Expires 5 seconds from now
        val futureTime = System.currentTimeMillis() + 5000L
        val activeEvent = SwarmEvent(
            speciesName = "pidgey", dimension = mockDimension, centerPos = centerPos, radius = 10, expireAt = futureTime
        )
        Assertions.assertFalse(
            activeEvent.isExpired(),
            "Event should remain active if expiration time is in the future."
        )
    }

    @Test
    fun `test default parameters initialize correctly`() {
        val mockDimension = mock<ResourceKey<Level>>()
        val centerPos = BlockPos.ZERO
        val event = SwarmEvent(
            speciesName = "rattata", dimension = mockDimension, centerPos = centerPos, radius = 15, expireAt = 0L
        )

        Assertions.assertNotNull(event.id)
        Assertions.assertEquals(5, event.minLevel)
        Assertions.assertEquals(15, event.maxLevel)
        Assertions.assertEquals(20, event.maxActiveEntities)
        Assertions.assertTrue(event.activeEntities.isEmpty())
    }
}