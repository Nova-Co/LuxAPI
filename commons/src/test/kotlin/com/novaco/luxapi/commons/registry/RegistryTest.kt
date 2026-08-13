package com.novaco.luxapi.commons.registry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RegistryTest {

    @Test
    fun `test register and get round trip`() {
        val registry = Registry<String, Int>()

        registry.register("one", 1)

        assertEquals(1, registry.get("one"))
        assertTrue(registry.has("one"))
        assertNull(registry.get("missing"))
        assertFalse(registry.has("missing"))
    }

    @Test
    fun `test register overwrites an existing entry for the same key`() {
        val registry = Registry<String, Int>()

        registry.register("key", 1)
        registry.register("key", 2)

        assertEquals(2, registry.get("key"))
        assertEquals(1, registry.size())
    }

    @Test
    fun `test unregister removes the entry`() {
        val registry = Registry<String, Int>()
        registry.register("key", 1)

        registry.unregister("key")

        assertNull(registry.get("key"))
        assertEquals(0, registry.size())
    }

    @Test
    fun `test all returns a snapshot unaffected by later mutation`() {
        val registry = Registry<String, Int>()
        registry.register("a", 1)

        val snapshot = registry.all()
        registry.register("b", 2)

        assertEquals(mapOf("a" to 1), snapshot)
        assertEquals(2, registry.size())
    }

    @Test
    fun `test clear empties the registry`() {
        val registry = Registry<String, Int>()
        registry.register("a", 1)
        registry.register("b", 2)

        registry.clear()

        assertEquals(0, registry.size())
    }
}
