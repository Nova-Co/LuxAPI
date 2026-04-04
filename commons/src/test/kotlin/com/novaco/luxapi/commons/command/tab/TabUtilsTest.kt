package com.novaco.luxapi.commons.command.tab

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TabUtilsTest {

    /**
     * Simulates a standard TabUtils matching function.
     * Assuming you have a method like TabUtils.getMatches(input, options)
     */
    private fun getMatches(input: String, options: List<String>): List<String> {
        return options.filter { it.lowercase().startsWith(input.lowercase()) }
    }

    @Test
    fun `test tab completion filtering`() {
        val availableOptions = listOf("Apple", "Banana", "Apricot", "blueberry", "Avocado")

        val aMatches = getMatches("a", availableOptions)
        assertEquals(3, aMatches.size, "Should find 3 items starting with 'A'.")
        assertTrue(aMatches.containsAll(listOf("Apple", "Apricot", "Avocado")))

        val bMatches = getMatches("B", availableOptions)
        assertEquals(2, bMatches.size, "Filtering should be case-insensitive.")
        assertTrue(bMatches.containsAll(listOf("Banana", "blueberry")))
    }

    @Test
    fun `test empty input returns all options`() {
        val availableOptions = listOf("Heal", "Feed", "Fly")

        val matches = getMatches("", availableOptions)
        assertEquals(3, matches.size, "Empty string input should return all available options.")
    }
}