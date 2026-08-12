package com.novaco.luxapi.cobblemon.gts

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class GlobalTradeManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            // Same trick as PCStorageManagerTest: mocking Pokemon triggers class init
            // that touches vanilla registries, which need Bootstrap to have run first.
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @BeforeEach
    fun resetCobblemonConfig() {
        val configField = Cobblemon::class.java.getDeclaredField("config")
        configField.isAccessible = true
        configField.set(Cobblemon, CobblemonConfig())
    }

    @Test
    fun `listPokemonCore serializes and removes the pokemon from the seller party`() {
        val party = mock<PlayerPartyStore>()
        val pokemon = mock<Pokemon>()
        val pokemonUuid = UUID.randomUUID()
        val sellerUuid = UUID.randomUUID()
        whenever(pokemon.uuid).thenReturn(pokemonUuid)
        whenever(party.get(pokemonUuid)).thenReturn(pokemon)
        whenever(party.occupied()).thenReturn(3)
        val listings = mutableMapOf<UUID, TradeListing>()

        val result = listPokemonCore(
            party, pokemon, sellerUuid, "Merchant", 150.0, listings,
            serialize = { "real_serialized_data" }
        )

        assertTrue(result)
        verify(party).remove(pokemon)
        assertEquals(1, listings.size)
        val listing = listings.values.first()
        assertEquals(sellerUuid, listing.sellerUuid)
        assertEquals("Merchant", listing.sellerName)
        assertEquals("real_serialized_data", listing.pokemonBase64)
        assertEquals(150.0, listing.price)
    }

    @Test
    fun `listPokemonCore fails when the pokemon is not in the seller party`() {
        val party = mock<PlayerPartyStore>()
        val pokemon = mock<Pokemon>()
        val pokemonUuid = UUID.randomUUID()
        whenever(pokemon.uuid).thenReturn(pokemonUuid)
        whenever(party.get(pokemonUuid)).thenReturn(null)
        val listings = mutableMapOf<UUID, TradeListing>()

        val result = listPokemonCore(
            party, pokemon, UUID.randomUUID(), "Merchant", 150.0, listings,
            serialize = { "unused" }
        )

        assertFalse(result)
        verify(party, never()).remove(pokemon)
        assertTrue(listings.isEmpty())
    }

    @Test
    fun `listPokemonCore fails when it would empty the party and server disallows it`() {
        Cobblemon.config.preventCompletePartyDeposit = true
        val party = mock<PlayerPartyStore>()
        val pokemon = mock<Pokemon>()
        val pokemonUuid = UUID.randomUUID()
        whenever(pokemon.uuid).thenReturn(pokemonUuid)
        whenever(party.get(pokemonUuid)).thenReturn(pokemon)
        whenever(party.occupied()).thenReturn(1)
        val listings = mutableMapOf<UUID, TradeListing>()

        val result = listPokemonCore(
            party, pokemon, UUID.randomUUID(), "Merchant", 150.0, listings,
            serialize = { "unused" }
        )

        assertFalse(result)
        verify(party, never()).remove(pokemon)
        assertTrue(listings.isEmpty())
    }

    @Test
    fun `grantPokemon prefers the party when it has space`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        whenever(party.getFirstAvailablePosition()).thenReturn(PartyPosition(0))
        whenever(party.add(pokemon)).thenReturn(true)

        val result = grantPokemon(party, pc, pokemon)

        assertTrue(result)
        verify(party).add(pokemon)
        verify(pc, never()).add(pokemon)
    }

    @Test
    fun `grantPokemon falls back to the pc when the party is full`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        whenever(party.getFirstAvailablePosition()).thenReturn(null)
        whenever(pc.add(pokemon)).thenReturn(true)

        val result = grantPokemon(party, pc, pokemon)

        assertTrue(result)
        verify(party, never()).add(pokemon)
        verify(pc).add(pokemon)
    }

    @Test
    fun `grantPokemon fails when both party and pc are full`() {
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val pokemon = mock<Pokemon>()
        whenever(party.getFirstAvailablePosition()).thenReturn(null)
        whenever(pc.add(pokemon)).thenReturn(false)

        val result = grantPokemon(party, pc, pokemon)

        assertFalse(result)
    }
}
