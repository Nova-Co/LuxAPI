package com.novaco.luxapi.commons.command.tab

import com.novaco.luxapi.commons.command.injector.impl.DummyCommandSender
import com.novaco.luxapi.commons.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * A dummy class used as a key for the TabRegistry.
 * In a real scenario, this might be a Warp class or a Player class.
 */
class Warp

class TabRegistryTest {

    @Test
    fun `test tab handler registration and retrieval by class`() {
        // Create a mock handler using the correct interface method
        val mockHandler = object : TabHandler {
            override fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> {
                return listOf("spawn", "shop", "pvp")
            }
        }

        // Register using the Class type as the key
        TabRegistry.register(Warp::class.java, mockHandler)

        // Retrieve using the Class type
        val retrieved = TabRegistry.getHandler(Warp::class.java)

        assertNotNull(retrieved, "Registry must return the handler associated with the Warp class.")

        // Test the suggestion logic
        val sender = DummyCommandSender()
        val suggestions = retrieved?.getSuggestions(sender, emptyArray())

        assertEquals(3, suggestions?.size, "Should return exactly 3 suggestions.")
        assertTrue(suggestions?.contains("shop") == true, "Suggestions should contain 'shop'.")
    }

    @Test
    fun `test tab utils filtering logic`() {
        val rawSuggestions = listOf("Apple", "Banana", "Apricot", "Blueberry")

        // Simulate typing "ap"
        val args = arrayOf("ap")
        val filtered = TabUtils.filter(rawSuggestions, args)

        assertEquals(2, filtered.size, "Should only return 'Apple' and 'Apricot'.")
        assertTrue(filtered.all { it.lowercase().startsWith("ap") }, "All results must start with the input prefix.")
    }

    @Test
    fun `test tab utils case insensitivity`() {
        val rawSuggestions = listOf("SPAwn", "Shop", "PvP")

        // Simulate typing "spa"
        val args = arrayOf("spa")
        val filtered = TabUtils.filter(rawSuggestions, args)

        assertEquals(1, filtered.size)
        assertEquals("SPAwn", filtered[0], "Filtering should be case-insensitive.")
    }
}