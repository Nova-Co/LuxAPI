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

    /**
     * A dummy provider whose placeholder expands into multiple lines, used exclusively for
     * testing [PlaceholderManager.replaceLines]. Uses a unique identifier ("stats") so it
     * never collides with [DummyProvider]'s "dummy" registration.
     */
    class DummyStatsProvider : MultiLinePlaceholderProvider {

        override fun identifier(): String = "stats"

        override fun onPlaceholderRequest(player: LuxPlayer?, params: String): String? {
            return if (params.lowercase() == "summary") "3 stats" else null
        }

        override fun onMultiLinePlaceholderRequest(player: LuxPlayer?, params: String): List<String>? {
            return if (params.lowercase() == "block") listOf("Kills: 10", "Deaths: 2", "K/D: 5.0") else null
        }
    }

    @BeforeEach
    fun setup() {
        // Register the dummy providers before running the tests
        PlaceholderManager.register(DummyProvider())
        PlaceholderManager.register(DummyStatsProvider())
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

    @Test
    fun `test replaceLines expands a full-line multi-line placeholder into several lines`() {
        val lines = listOf("Header", "%stats_block%", "Footer")

        val result = PlaceholderManager.replaceLines(null, lines)

        assertEquals(listOf("Header", "Kills: 10", "Deaths: 2", "K/D: 5.0", "Footer"), result)
    }

    @Test
    fun `test replaceLines resolves an inline placeholder within a larger line as single-line`() {
        val lines = listOf("Summary: %stats_summary%")

        val result = PlaceholderManager.replaceLines(null, lines)

        assertEquals(listOf("Summary: 3 stats"), result)
    }

    @Test
    fun `test replaceLines falls back to single-line replace for a non-multi-line provider`() {
        val lines = listOf("%dummy_hello%")

        val result = PlaceholderManager.replaceLines(null, lines)

        assertEquals(listOf("world"), result)
    }

    @Test
    fun `test replaceLines leaves unregistered placeholder lines unchanged`() {
        val lines = listOf("%unregistered_test%")

        val result = PlaceholderManager.replaceLines(null, lines)

        assertEquals(listOf("%unregistered_test%"), result)
    }
}