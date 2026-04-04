package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlaceholderManagerTest {

    /**
     * A dummy provider used exclusively for testing purposes.
     * Implements the exact signatures from the PlaceholderProvider interface.
     */
    class DummyProvider : PlaceholderProvider {

        override fun identifier(): String {
            return "dummy"
        }

        override fun onPlaceholderRequest(player: LuxPlayer?, params: String): String? {
            return when (params.lowercase()) {
                "hello" -> "world"
                "number" -> "42"
                else -> null
            }
        }
    }

    @BeforeEach
    fun setup() {
        // Register the dummy provider before running the tests
        PlaceholderManager.register(DummyProvider())
    }

    @Test
    fun `test successful placeholder replacement`() {
        val rawText = "Hello %dummy_hello%, the answer is %dummy_number%!"

        // Passing null for LuxPlayer since our DummyProvider doesn't strict-check it
        val result = PlaceholderManager.replace(null, rawText)

        assertEquals("Hello world, the answer is 42!", result, "Placeholders should be fully replaced")
    }

    @Test
    fun `test invalid or unregistered placeholders remain unchanged`() {
        val rawText = "This %dummy_unknown% and %unregistered_test% should stay."

        val result = PlaceholderManager.replace(null, rawText)

        assertEquals(rawText, result, "Unknown placeholders should not be modified and should output their raw text")
    }
}