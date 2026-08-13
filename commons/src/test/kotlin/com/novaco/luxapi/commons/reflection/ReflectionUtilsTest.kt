package com.novaco.luxapi.commons.reflection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

annotation class DummyMarker

open class ReflectionBase {
    @field:DummyMarker
    private var inheritedField: String = "base-value"
}

class ReflectionTarget : ReflectionBase() {
    @field:DummyMarker
    private var name: String = "initial"
    private var untouched: Int = 1

    @DummyMarker
    fun annotatedMethod() {}
    fun plainMethod() {}
}

class ReflectionUtilsTest {

    @Test
    fun `test newInstance constructs via no-arg constructor`() {
        val instance = ReflectionUtils.newInstance(ReflectionTarget::class.java)

        assertNotNull(instance)
        assertEquals("initial", ReflectionUtils.getFieldValue(instance!!, "name"))
    }

    @Test
    fun `test newInstance returns null when no no-arg constructor exists`() {
        class NoDefaultConstructor(val value: Int)

        assertNull(ReflectionUtils.newInstance(NoDefaultConstructor::class.java))
    }

    @Test
    fun `test getFieldValue and setFieldValue access private fields including inherited ones`() {
        val target = ReflectionTarget()

        assertEquals("initial", ReflectionUtils.getFieldValue(target, "name"))
        assertEquals("base-value", ReflectionUtils.getFieldValue(target, "inheritedField"))

        assertTrue(ReflectionUtils.setFieldValue(target, "name", "updated"))
        assertEquals("updated", ReflectionUtils.getFieldValue(target, "name"))
    }

    @Test
    fun `test getFieldValue and setFieldValue return null or false for unknown fields`() {
        val target = ReflectionTarget()

        assertNull(ReflectionUtils.getFieldValue(target, "doesNotExist"))
        assertFalse(ReflectionUtils.setFieldValue(target, "doesNotExist", "x"))
    }

    @Test
    fun `test findAnnotatedFields and findAnnotatedMethods only return annotated members`() {
        val fields = ReflectionUtils.findAnnotatedFields(ReflectionTarget::class.java, DummyMarker::class.java)
        val methods = ReflectionUtils.findAnnotatedMethods(ReflectionTarget::class.java, DummyMarker::class.java)

        assertEquals(listOf("name"), fields.map { it.name })
        assertEquals(listOf("annotatedMethod"), methods.map { it.name })
    }
}
