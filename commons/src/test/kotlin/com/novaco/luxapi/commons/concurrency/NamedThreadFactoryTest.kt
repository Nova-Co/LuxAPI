package com.novaco.luxapi.commons.concurrency

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NamedThreadFactoryTest {

    @Test
    fun `test threads are named with an incrementing suffix`() {
        val factory = NamedThreadFactory("Lux-Worker")

        val first = factory.newThread {}
        val second = factory.newThread {}

        assertEquals("Lux-Worker-1", first.name)
        assertEquals("Lux-Worker-2", second.name)
    }

    @Test
    fun `test threads are daemon by default`() {
        val factory = NamedThreadFactory("Lux-Worker")

        assertTrue(factory.newThread {}.isDaemon)
    }

    @Test
    fun `test daemon flag can be disabled`() {
        val factory = NamedThreadFactory("Lux-Worker", daemon = false)

        assertFalse(factory.newThread {}.isDaemon)
    }
}
