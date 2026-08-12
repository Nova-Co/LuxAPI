package com.novaco.luxapi.cobblemon.storage

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCBox
import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class PCStorageManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            // Mocking com.cobblemon.mod.common.pokemon.Pokemon triggers its class init,
            // whose ROOT_CODEC touches ItemStack's vanilla registries. Those require
            // Minecraft's Bootstrap to have run first, same as PokemonPropertyManagerTest.
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @BeforeEach
    fun resetCobblemonConfig() {
        // Cobblemon.config is `lateinit`, only populated by real mod bootstrap.
        // Reflection-inject a plain default config so preventCompletePartyDeposit
        // reads/writes safely in a headless unit test, same trick already used in
        // EvolutionHookManagerTest for other Cobblemon singleton internals.
        val configField = Cobblemon::class.java.getDeclaredField("config")
        configField.isAccessible = true
        configField.set(Cobblemon, CobblemonConfig())
    }

    @Test
    fun `deposit to first available slot removes from party and adds to pc`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        val target = PCPosition(0, 0)
        whenever(party.get(0)).thenReturn(pokemon)
        whenever(party.occupied()).thenReturn(2)
        whenever(pc.getFirstAvailablePosition()).thenReturn(target)

        val result = depositToPCCore(party, pc, 0, null, null)

        assertTrue(result)
        verify(party).remove(pokemon)
        verify(pc).set(target, pokemon)
    }

    @Test
    fun `deposit returns false when the party slot is empty`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        whenever(party.get(3)).thenReturn(null)

        val result = depositToPCCore(party, pc, 3, null, null)

        assertFalse(result)
        verifyNoInteractions(pc)
    }

    @Test
    fun `deposit blocked when it would empty the party and server disallows it`() {
        Cobblemon.config.preventCompletePartyDeposit = true
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        whenever(party.get(0)).thenReturn(pokemon)
        whenever(party.occupied()).thenReturn(1)

        val result = depositToPCCore(party, pc, 0, null, null)

        assertFalse(result)
        verifyNoInteractions(pc)
    }

    @Test
    fun `deposit to an explicit occupied slot fails loud instead of redirecting`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        val occupant = mock<Pokemon>()
        val box0 = mock<PCBox>()
        whenever(party.get(0)).thenReturn(pokemon)
        whenever(party.occupied()).thenReturn(2)
        whenever(pc.boxes).thenReturn(mutableListOf(box0))
        whenever(pc[PCPosition(0, 5)]).thenReturn(occupant)

        val result = depositToPCCore(party, pc, 0, 0, 5)

        assertFalse(result)
        verify(party, never()).remove(pokemon)
        verify(pc, never()).set(PCPosition(0, 5), pokemon)
    }

    @Test
    fun `deposit to an explicit out-of-bounds box fails`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        whenever(party.get(0)).thenReturn(pokemon)
        whenever(party.occupied()).thenReturn(2)
        whenever(pc.boxes).thenReturn(mutableListOf<PCBox>())

        val result = depositToPCCore(party, pc, 0, 0, 0)

        assertFalse(result)
        verify(party, never()).remove(pokemon)
    }

    @Test
    fun `withdraw to first available party slot removes from pc and adds to party`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        val source = PCPosition(1, 2)
        val target = PartyPosition(3)
        whenever(pc.boxes).thenReturn(mutableListOf(mock<PCBox>(), mock()))
        whenever(pc[source]).thenReturn(pokemon)
        whenever(party.getFirstAvailablePosition()).thenReturn(target)

        val result = withdrawFromPCCore(party, pc, 1, 2, null)

        assertTrue(result)
        verify(pc).remove(source)
        verify(party).set(target, pokemon)
    }

    @Test
    fun `withdraw returns false when the pc slot is empty`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        whenever(pc.boxes).thenReturn(mutableListOf(mock<PCBox>()))
        whenever(pc[PCPosition(0, 0)]).thenReturn(null)

        val result = withdrawFromPCCore(party, pc, 0, 0, null)

        assertFalse(result)
        verifyNoInteractions(party)
    }

    @Test
    fun `withdraw returns false when the pc box is out of bounds`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        whenever(pc.boxes).thenReturn(mutableListOf<PCBox>())

        val result = withdrawFromPCCore(party, pc, 5, 0, null)

        assertFalse(result)
        verifyNoInteractions(party)
    }

    @Test
    fun `withdraw to an explicit occupied party slot fails loud`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        val occupant = mock<Pokemon>()
        val source = PCPosition(0, 0)
        whenever(pc.boxes).thenReturn(mutableListOf(mock<PCBox>()))
        whenever(pc[source]).thenReturn(pokemon)
        whenever(party.size()).thenReturn(6)
        whenever(party.get(2)).thenReturn(occupant)

        val result = withdrawFromPCCore(party, pc, 0, 0, 2)

        assertFalse(result)
        verify(pc, never()).remove(source)
    }

    @Test
    fun `withdraw returns false when party is full and no slot is available`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        val source = PCPosition(0, 0)
        whenever(pc.boxes).thenReturn(mutableListOf(mock<PCBox>()))
        whenever(pc[source]).thenReturn(pokemon)
        whenever(party.getFirstAvailablePosition()).thenReturn(null)

        val result = withdrawFromPCCore(party, pc, 0, 0, null)

        assertFalse(result)
        verify(pc, never()).remove(source)
    }
}
