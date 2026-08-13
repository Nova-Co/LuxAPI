package com.novaco.luxapi.cobblemon.types

import com.cobblemon.mod.common.api.types.tera.TeraTypes
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.network.chat.Component
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ElementalTypeManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `get resolves a built-in type by name`() {
        assertEquals("Fire", ElementalTypeManager.get("fire")?.name)
    }

    @Test
    fun `get returns null for an unknown type name`() {
        assertNull(ElementalTypeManager.get("not_a_real_lux_type"))
    }

    @Test
    fun `register adds a new type that get can then resolve`() {
        val type = ElementalTypeManager.register(
            "LuxTestType",
            Component.literal("Lux Test Type"),
            0x123456,
            99
        )

        assertEquals(type, ElementalTypeManager.get("LuxTestType"))
        assertTrue(ElementalTypeManager.all().contains(type))
    }

    @Test
    fun `getTeraType resolves a built-in Tera type by id`() {
        assertEquals(TeraTypes.FIRE, ElementalTypeManager.getTeraType("fire"))
    }

    @Test
    fun `setTeraType assigns a known Tera type and reports success`() {
        val pokemon = mock<Pokemon>()

        val result = ElementalTypeManager.setTeraType(pokemon, "water")

        assertTrue(result)
        verify(pokemon).teraType = TeraTypes.WATER
    }

    @Test
    fun `setTeraType rejects an unknown id without touching the Pokemon`() {
        val pokemon = mock<Pokemon>()

        val result = ElementalTypeManager.setTeraType(pokemon, "not_a_real_lux_tera_type")

        assertFalse(result)
    }
}
