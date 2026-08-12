package com.novaco.luxapi.cobblemon.progression

import com.cobblemon.mod.common.api.pokedex.AbstractPokedexManager
import com.cobblemon.mod.common.api.pokedex.CaughtCount
import com.cobblemon.mod.common.api.pokedex.CaughtPercent
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokedex.SeenCount
import net.minecraft.resources.ResourceLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PokedexManagerTest {

    private val pikachuId = ResourceLocation.parse("cobblemon:pikachu")
    private val kantoDexId = ResourceLocation.parse("cobblemon:kanto")

    @Test
    fun `hasCaughtCore is true only when knowledge is CAUGHT`() {
        val dexManager = mock<AbstractPokedexManager>()
        whenever(dexManager.getKnowledgeForSpecies(pikachuId)).thenReturn(PokedexEntryProgress.CAUGHT)

        assertTrue(hasCaughtCore(dexManager, pikachuId))
    }

    @Test
    fun `hasCaughtCore is false when only encountered`() {
        val dexManager = mock<AbstractPokedexManager>()
        whenever(dexManager.getKnowledgeForSpecies(pikachuId)).thenReturn(PokedexEntryProgress.ENCOUNTERED)

        assertFalse(hasCaughtCore(dexManager, pikachuId))
    }

    @Test
    fun `hasSeenCore is true for encountered, false for none`() {
        val encounteredManager = mock<AbstractPokedexManager>()
        whenever(encounteredManager.getKnowledgeForSpecies(pikachuId)).thenReturn(PokedexEntryProgress.ENCOUNTERED)
        val unseenManager = mock<AbstractPokedexManager>()
        whenever(unseenManager.getKnowledgeForSpecies(pikachuId)).thenReturn(PokedexEntryProgress.NONE)

        assertTrue(hasSeenCore(encounteredManager, pikachuId))
        assertFalse(hasSeenCore(unseenManager, pikachuId))
    }

    @Test
    fun `getCaughtCountCore delegates to the global calculator when dexId is null`() {
        val dexManager = mock<AbstractPokedexManager>()
        whenever(dexManager.getGlobalCalculatedValue(CaughtCount)).thenReturn(42)

        assertEquals(42, getCaughtCountCore(dexManager, null))
    }

    @Test
    fun `getCaughtCountCore delegates to the dex-scoped calculator when dexId is given`() {
        val dexManager = mock<AbstractPokedexManager>()
        whenever(dexManager.getDexCalculatedValue(kantoDexId, CaughtCount)).thenReturn(7)

        assertEquals(7, getCaughtCountCore(dexManager, kantoDexId))
    }

    @Test
    fun `getSeenCountCore delegates to the right calculator scope`() {
        val dexManager = mock<AbstractPokedexManager>()
        whenever(dexManager.getGlobalCalculatedValue(SeenCount)).thenReturn(100)
        whenever(dexManager.getDexCalculatedValue(kantoDexId, SeenCount)).thenReturn(15)

        assertEquals(100, getSeenCountCore(dexManager, null))
        assertEquals(15, getSeenCountCore(dexManager, kantoDexId))
    }

    @Test
    fun `getCompletionPercentageCore delegates to CaughtPercent and widens to Double`() {
        val dexManager = mock<AbstractPokedexManager>()
        whenever(dexManager.getGlobalCalculatedValue(CaughtPercent)).thenReturn(33.5F)
        whenever(dexManager.getDexCalculatedValue(kantoDexId, CaughtPercent)).thenReturn(80.0F)

        assertEquals(33.5, getCompletionPercentageCore(dexManager, null), 0.001)
        assertEquals(80.0, getCompletionPercentageCore(dexManager, kantoDexId), 0.001)
    }
}
