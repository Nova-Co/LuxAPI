package com.novaco.luxapi.commons.type

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExpiringMapTest {

    @Test
    fun `test set entry is active and reports remaining time`() {
        val map = ExpiringMap<String>()
        map.set("a", 5000L)

        assertTrue(map.isActive("a"))
        assertTrue(map.remaining("a") > 0)
    }

    @Test
    fun `test entry expires after its duration elapses`() {
        val map = ExpiringMap<String>()
        map.set("a", 20L)

        Thread.sleep(30)

        assertFalse(map.isActive("a"))
        assertEquals(0L, map.remaining("a"))
    }

    @Test
    fun `test clear removes an entry immediately`() {
        val map = ExpiringMap<String>()
        map.set("a", 10000L)
        assertTrue(map.isActive("a"))

        map.clear("a")

        assertFalse(map.isActive("a"))
    }

    @Test
    fun `test cleanUp drops only expired entries`() {
        val map = ExpiringMap<String>()
        map.set("expired", 10L)
        map.set("active", 10000L)

        Thread.sleep(20)
        map.cleanUp()

        assertFalse(map.isActive("expired"))
        assertTrue(map.isActive("active"))
    }

    @Test
    fun `test unknown key is inactive with zero remaining time`() {
        val map = ExpiringMap<String>()

        assertFalse(map.isActive("missing"))
        assertEquals(0L, map.remaining("missing"))
    }
}
