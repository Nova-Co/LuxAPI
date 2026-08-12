package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PokemonPropertyManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `setIV applies an in-range value`() {
        val pokemon = mock<Pokemon>()
        val ivs = IVs()
        whenever(pokemon.ivs).thenReturn(ivs)
        // Pokemon.setIV is a real Cobblemon member, but calling it on a mock is a
        // no-op unless stubbed — simulate its real side effect (mutating ivs) so we
        // can verify PokemonPropertyManager's readback-based success detection.
        doAnswer { ivs[Stats.SPEED] = 31 }.whenever(pokemon).setIV(Stats.SPEED, 31)

        val result = PokemonPropertyManager.setIV(pokemon, Stats.SPEED, 31)

        assertTrue(result)
        assertEquals(31, ivs.getOrDefault(Stats.SPEED))
    }

    @Test
    fun `setIV rejects an out-of-range value`() {
        val pokemon = mock<Pokemon>()
        val ivs = IVs()
        whenever(pokemon.ivs).thenReturn(ivs)

        val result = PokemonPropertyManager.setIV(pokemon, Stats.SPEED, 32)

        assertFalse(result)
        assertEquals(0, ivs.getOrDefault(Stats.SPEED))
    }

    @Test
    fun `setEV applies an in-range value`() {
        val pokemon = mock<Pokemon>()
        val evs = EVs()
        whenever(pokemon.evs).thenReturn(evs)
        // Pokemon.setEV is a real Cobblemon member, but calling it on a mock is a
        // no-op unless stubbed — simulate its real side effect (mutating evs) so we
        // can verify PokemonPropertyManager's readback-based success detection.
        doAnswer { evs[Stats.ATTACK] = 252 }.whenever(pokemon).setEV(Stats.ATTACK, 252)

        val result = PokemonPropertyManager.setEV(pokemon, Stats.ATTACK, 252)

        assertTrue(result)
        assertEquals(252, evs.getOrDefault(Stats.ATTACK))
    }

    @Test
    fun `setEV rejects a value that would exceed the 510 total cap`() {
        val pokemon = mock<Pokemon>()
        val evs = EVs()
        evs[Stats.ATTACK] = 252
        evs[Stats.DEFENCE] = 252
        whenever(pokemon.evs).thenReturn(evs)

        // 252 + 252 + 10 = 514 > 510 total cap, must be rejected
        val result = PokemonPropertyManager.setEV(pokemon, Stats.SPEED, 10)

        assertFalse(result)
        assertEquals(0, evs.getOrDefault(Stats.SPEED))
    }

    @Test
    fun `setNature applies a valid nature id`() {
        val pokemon = mock<Pokemon>()

        val result = PokemonPropertyManager.setNature(pokemon, "adamant")

        assertTrue(result)
        verify(pokemon).nature = Natures.getNature("adamant")!!
    }

    @Test
    fun `setNature rejects an unknown nature id`() {
        val pokemon = mock<Pokemon>()

        val result = PokemonPropertyManager.setNature(pokemon, "not_a_real_nature")

        assertFalse(result)
        verify(pokemon, never()).nature = any()
    }

    @Test
    fun `setHeldItem defaults decrement to false`() {
        val pokemon = mock<Pokemon>()
        val newItem = mock<ItemStack>()
        val oldItem = mock<ItemStack>()
        whenever(pokemon.swapHeldItem(newItem, false, true)).thenReturn(oldItem)

        val result = PokemonPropertyManager.setHeldItem(pokemon, newItem)

        assertEquals(oldItem, result)
    }
}
