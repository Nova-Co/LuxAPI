package com.novaco.luxapi.commons.registry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class ClassValueRegistryTest {

    @Test
    fun `test value is computed lazily from the class`() {
        val registry = ClassValueRegistry<String> { it.simpleName }

        assertEquals("String", registry.get(String::class.java))
        assertEquals("Integer", registry.get(Integer::class.java))
    }

    @Test
    fun `test factory only runs once per class even across repeated get calls`() {
        val calls = AtomicInteger(0)
        val registry = ClassValueRegistry<Int> { calls.incrementAndGet() }

        registry.get(String::class.java)
        registry.get(String::class.java)
        registry.get(String::class.java)

        assertEquals(1, calls.get())
    }

    @Test
    fun `test remove forces recomputation on next get`() {
        val calls = AtomicInteger(0)
        val registry = ClassValueRegistry<Int> { calls.incrementAndGet() }

        registry.get(String::class.java)
        registry.remove(String::class.java)
        registry.get(String::class.java)

        assertEquals(2, calls.get())
    }
}
