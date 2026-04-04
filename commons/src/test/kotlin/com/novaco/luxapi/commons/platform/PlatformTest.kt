package com.novaco.luxapi.commons.platform

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlatformTest {

    @Test
    fun `test platform initialization and assignment`() {
        LuxPlatform.type = PlatformType.NEOFORGE

        assertEquals(PlatformType.NEOFORGE, LuxPlatform.type, "Platform should be NEOFORGE")

        assertTrue(LuxPlatform.isNeoForge(), "isNeoForge() should return true")
        assertFalse(LuxPlatform.isFabric(), "isFabric() should return false when on NeoForge")
    }

    @Test
    fun `test platform switching to fabric`() {
        LuxPlatform.type = PlatformType.FABRIC

        assertEquals(PlatformType.FABRIC, LuxPlatform.type, "Platform should be FABRIC")
        assertTrue(LuxPlatform.isFabric(), "isFabric() should return true")
        assertFalse(LuxPlatform.isNeoForge(), "isNeoForge() should return false when on Fabric")
    }
}