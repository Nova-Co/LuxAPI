package com.novaco.luxapi.cobblemon.tag

import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TagManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `labelsOf returns the species' own labels`() {
        val species = mock<Species>()
        whenever(species.labels).thenReturn(hashSetOf("legendary", "gen1"))

        assertEquals(setOf("legendary", "gen1"), TagManager.labelsOf(species))
    }

    @Test
    fun `hasLabel matches case-insensitively`() {
        val species = mock<Species>()
        whenever(species.labels).thenReturn(hashSetOf("Legendary"))

        assertTrue(TagManager.hasLabel(species, "legendary"))
    }

    @Test
    fun `hasLabel is false when the species lacks the label`() {
        val species = mock<Species>()
        whenever(species.labels).thenReturn(hashSetOf("gen1"))

        assertFalse(TagManager.hasLabel(species, "legendary"))
    }

    @Test
    fun `speciesWithLabel finds nothing when PokemonSpecies has no implemented species loaded`() {
        // PokemonSpecies is a JSON-loaded registry, empty in a unit test.
        assertTrue(TagManager.speciesWithLabel("legendary").isEmpty())
    }
}
