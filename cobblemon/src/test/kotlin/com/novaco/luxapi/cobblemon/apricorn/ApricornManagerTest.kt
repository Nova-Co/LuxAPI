package com.novaco.luxapi.cobblemon.apricorn

import com.cobblemon.mod.common.api.apricorn.Apricorn
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ApricornManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `all returns every apricorn variety`() {
        assertEquals(7, ApricornManager.all().size)
        assertTrue(ApricornManager.all().contains(Apricorn.RED))
    }

    @Test
    fun `get resolves a variety by name case-insensitively`() {
        assertEquals(Apricorn.BLUE, ApricornManager.get("blue"))
        assertEquals(Apricorn.BLUE, ApricornManager.get("BLUE"))
    }

    @Test
    fun `get returns null for an unknown variety name`() {
        assertNull(ApricornManager.get("not_a_real_lux_apricorn"))
    }

    // itemFor/blockFor/seedFor are thin passthroughs to Apricorn's own item()/block()/seed()
    // accessors, which trigger CobblemonItems/CobblemonBlocks static init (real Minecraft
    // item/block registration) — that fails outside a live registry context
    // ("This registry can't create intrusive holders"), so they aren't exercisable in a
    // bare unit test the way the rest of this wrapper is.
}
