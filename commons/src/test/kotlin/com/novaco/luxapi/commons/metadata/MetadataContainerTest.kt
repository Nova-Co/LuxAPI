package com.novaco.luxapi.commons.metadata

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MetadataContainerTest {

    private lateinit var container: MetadataContainer

    @BeforeEach
    fun setup() {
        container = MetadataContainer()
    }

    @Test
    fun `test storing and retrieving multiple data types`() {
        // Using .set() instead of .put()
        container.set("player_kills", 150)
        container.set("current_quest", "dragon_slayer")
        container.set("is_vip", true)

        assertTrue(container.has("player_kills"), "Container should have player_kills key")

        // Passing the class type into the get() method.
        // Note: We use javaObjectType for primitives to avoid reflection mismatch issues.
        assertEquals(150, container.get("player_kills", Int::class.javaObjectType), "Should retrieve the correct integer")
        assertEquals("dragon_slayer", container.get("current_quest", String::class.java), "Should retrieve the correct string")
        assertEquals(true, container.get("is_vip", Boolean::class.javaObjectType), "Should retrieve the correct boolean")
    }

    @Test
    fun `test safe casting returns null on mismatch`() {
        container.set("score", 100)

        // Requesting the wrong type should safely return null, not throw a ClassCastException
        val wrongType = container.get("score", String::class.java)
        assertNull(wrongType, "Retrieving with the wrong class type should return null")
    }

    @Test
    fun `test removing data from container`() {
        container.set("temp_data", 999)
        assertTrue(container.has("temp_data"))

        container.remove("temp_data")
        assertFalse(container.has("temp_data"), "Data should be completely removed")
        assertNull(container.get("temp_data", Int::class.javaObjectType), "Retrieving removed data should return null")
    }
}