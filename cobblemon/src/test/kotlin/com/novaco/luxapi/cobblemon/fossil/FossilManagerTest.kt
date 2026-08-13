package com.novaco.luxapi.cobblemon.fossil

import com.cobblemon.mod.common.api.fossil.Fossil
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FossilManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    // Fossils is a JSON-loaded registry with no public register(), so it's empty in a
    // unit test — get()/matchFossil()/isFossilIngredient() are exercised against that
    // empty state, while createResult() is exercised against a directly-constructed
    // Fossil, matching MoveManagerTest's registry-free fallback pattern.

    @Test
    fun `get returns null for an unknown identifier in an unpopulated registry`() {
        assertNull(FossilManager.get(ResourceLocation.fromNamespaceAndPath("lux", "not_a_real_fossil")))
    }

    @Test
    fun `matchFossil finds nothing in an unpopulated registry`() {
        assertNull(FossilManager.matchFossil(listOf(mock<ItemStack>())))
    }

    @Test
    fun `isFossilIngredient is false in an unpopulated registry`() {
        assertFalse(FossilManager.isFossilIngredient(mock<ItemStack>()))
    }

    @Test
    fun `createResult delegates to the fossil's result properties`() {
        val properties = mock<PokemonProperties>()
        val pokemon = mock<Pokemon>()
        whenever(properties.create(null)).thenReturn(pokemon)
        val fossil = Fossil(
            ResourceLocation.fromNamespaceAndPath("lux", "test_fossil"),
            properties,
            emptyList()
        )

        val result = FossilManager.createResult(fossil)

        assertEquals(pokemon, result)
    }
}
