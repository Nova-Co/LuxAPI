package com.novaco.luxapi.cobblemon

import com.novaco.luxapi.cobblemon.listener.CobblemonEventHandler
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times

class LuxCobblemonTest {

    @Test
    @Disabled("Requires Cobblemon Event Bus. Run via Integration Testing.")
    fun `test init registers cobblemon event handler`() {
        // Intercept the static/singleton call to prevent triggering native Cobblemon APIs during testing
        mockStatic(CobblemonEventHandler::class.java).use { mockedHandler ->

            assertDoesNotThrow("LuxCobblemon.init() should execute without throwing exceptions.") {
                LuxCobblemon.init()
            }

            // Verify the registration method was called exactly once
            mockedHandler.verify(
                { CobblemonEventHandler.register() },
                times(1)
            )
        }
    }
}