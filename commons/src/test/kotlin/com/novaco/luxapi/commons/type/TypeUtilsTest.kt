package com.novaco.luxapi.commons.type

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

enum class DummyStatus { ACTIVE, INACTIVE }

class TypeUtilsTest {

    @Test
    fun `test safeCast returns null on type mismatch instead of throwing`() {
        val value: Any = "not a number"

        assertNull(TypeUtils.safeCast<Int>(value))
        assertEquals("not a number", TypeUtils.safeCast<String>(value))
    }

    @Test
    fun `test asInt parses numbers and numeric strings, falls back otherwise`() {
        assertEquals(5, TypeUtils.asInt(5))
        assertEquals(7, TypeUtils.asInt("7"))
        assertEquals(3, TypeUtils.asInt(3.9))
        assertEquals(-1, TypeUtils.asInt("not a number", -1))
        assertEquals(0, TypeUtils.asInt(null))
    }

    @Test
    fun `test asDouble and asLong parse numeric strings`() {
        assertEquals(3.5, TypeUtils.asDouble("3.5"))
        assertEquals(10L, TypeUtils.asLong("10"))
        assertEquals(-1.0, TypeUtils.asDouble("garbage", -1.0))
    }

    @Test
    fun `test asBoolean is case-insensitive and falls back on invalid input`() {
        assertTrue(TypeUtils.asBoolean("TRUE"))
        assertFalse(TypeUtils.asBoolean("false"))
        assertTrue(TypeUtils.asBoolean("garbage", true))
    }

    @Test
    fun `test asString stringifies non-null values and falls back on null`() {
        assertEquals("42", TypeUtils.asString(42))
        assertEquals("none", TypeUtils.asString(null, "none"))
    }

    @Test
    fun `test asEnum resolves case-insensitively and falls back on no match`() {
        assertEquals(DummyStatus.ACTIVE, TypeUtils.asEnum("active", DummyStatus.INACTIVE))
        assertEquals(DummyStatus.INACTIVE, TypeUtils.asEnum("unknown", DummyStatus.INACTIVE))
        assertEquals(DummyStatus.INACTIVE, TypeUtils.asEnum(null, DummyStatus.INACTIVE))
    }
}
