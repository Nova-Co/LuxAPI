package com.novaco.luxapi.cobblemon.gts

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.economy.TradeTaxManager
import com.novaco.luxapi.commons.economy.EconomyService
import com.novaco.luxapi.commons.player.LuxPlayer
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

private class FakeEconomyService : EconomyService {
    val balances = mutableMapOf<UUID, Double>()

    override fun getBalance(player: LuxPlayer): Double = balances.getOrDefault(player.uniqueId, 0.0)
    override fun hasEnough(player: LuxPlayer, amount: Double): Boolean = getBalance(player) >= amount
    override fun deposit(uuid: UUID, amount: Double): Boolean {
        balances[uuid] = balances.getOrDefault(uuid, 0.0) + amount
        return true
    }
    override fun withdraw(player: LuxPlayer, amount: Double): Boolean {
        if (!hasEnough(player, amount)) return false
        balances[player.uniqueId] = getBalance(player) - amount
        return true
    }
}

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

    @BeforeEach
    fun clearTradeTaxCalculators() {
        val field = TradeTaxManager::class.java.getDeclaredField("calculators")
        field.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val list = field.get(TradeTaxManager) as MutableList<Any>
        list.clear()
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

    @Test
    fun `purchaseListingCore grants the real pokemon and pays the seller`() {
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "serialized_data", 100.0)
        )
        val buyer = mock<LuxPlayer>()
        val buyerUuid = UUID.randomUUID()
        whenever(buyer.uniqueId).thenReturn(buyerUuid)
        val economy = FakeEconomyService()
        economy.balances[buyerUuid] = 200.0
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val realPokemon = mock<Pokemon>()
        whenever(party.getFirstAvailablePosition()).thenReturn(PartyPosition(0))
        whenever(party.add(realPokemon)).thenReturn(true)

        val result = purchaseListingCore(
            buyer, party, pc, listingId, listings, economy,
            deserialize = { data -> if (data == "serialized_data") realPokemon else null }
        )

        assertTrue(result is TradeResult.Success)
        assertEquals(realPokemon, (result as TradeResult.Success).pokemon)
        assertEquals(100.0, economy.balances[buyerUuid])
        assertEquals(100.0, economy.balances[sellerUuid])
        assertTrue(listings.isEmpty())
        verify(party).add(realPokemon)
    }

    @Test
    fun `purchaseListingCore deducts tax from the seller payout`() {
        TradeTaxManager.registerTaxCalculator { _, price, _ -> price * 0.20 }
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "serialized_data", 100.0)
        )
        val buyer = mock<LuxPlayer>()
        val buyerUuid = UUID.randomUUID()
        whenever(buyer.uniqueId).thenReturn(buyerUuid)
        val economy = FakeEconomyService()
        economy.balances[buyerUuid] = 200.0
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val realPokemon = mock<Pokemon>()
        whenever(party.getFirstAvailablePosition()).thenReturn(PartyPosition(0))
        whenever(party.add(realPokemon)).thenReturn(true)

        purchaseListingCore(buyer, party, pc, listingId, listings, economy, deserialize = { realPokemon })

        assertEquals(100.0, economy.balances[buyerUuid]) // paid full 100
        assertEquals(80.0, economy.balances[sellerUuid])  // received 100 - 20% tax
    }

    @Test
    fun `purchaseListingCore fails without touching funds when the listing is corrupt`() {
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "corrupt_data", 100.0)
        )
        val buyer = mock<LuxPlayer>()
        val buyerUuid = UUID.randomUUID()
        whenever(buyer.uniqueId).thenReturn(buyerUuid)
        val economy = FakeEconomyService()
        economy.balances[buyerUuid] = 200.0
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()

        val result = purchaseListingCore(buyer, party, pc, listingId, listings, economy, deserialize = { null })

        assertTrue(result is TradeResult.Failure)
        assertEquals(200.0, economy.balances[buyerUuid])
        assertEquals(1, listings.size) // listing left intact, not silently dropped
    }

    @Test
    fun `purchaseListingCore fails without charging when buyer has no space`() {
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "serialized_data", 100.0)
        )
        val buyer = mock<LuxPlayer>()
        val buyerUuid = UUID.randomUUID()
        whenever(buyer.uniqueId).thenReturn(buyerUuid)
        val economy = FakeEconomyService()
        economy.balances[buyerUuid] = 200.0
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val realPokemon = mock<Pokemon>()
        whenever(party.getFirstAvailablePosition()).thenReturn(null)
        whenever(pc.getFirstAvailablePosition()).thenReturn(null)

        val result = purchaseListingCore(buyer, party, pc, listingId, listings, economy, deserialize = { realPokemon })

        assertTrue(result is TradeResult.Failure)
        assertEquals(200.0, economy.balances[buyerUuid])
        assertEquals(1, listings.size)
    }

    @Test
    fun `purchaseListingCore fails on self-purchase`() {
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "serialized_data", 100.0)
        )
        val buyer = mock<LuxPlayer>()
        whenever(buyer.uniqueId).thenReturn(sellerUuid)
        val economy = FakeEconomyService()
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()

        val result = purchaseListingCore(buyer, party, pc, listingId, listings, economy, deserialize = { mock<Pokemon>() })

        assertTrue(result is TradeResult.Failure)
        assertEquals(1, listings.size)
    }

    @Test
    fun `purchaseListingCore fails on insufficient funds without removing the listing`() {
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "serialized_data", 100.0)
        )
        val buyer = mock<LuxPlayer>()
        val buyerUuid = UUID.randomUUID()
        whenever(buyer.uniqueId).thenReturn(buyerUuid)
        val economy = FakeEconomyService()
        economy.balances[buyerUuid] = 10.0
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        whenever(party.getFirstAvailablePosition()).thenReturn(PartyPosition(0))

        val result = purchaseListingCore(buyer, party, pc, listingId, listings, economy, deserialize = { mock<Pokemon>() })

        assertTrue(result is TradeResult.Failure)
        assertEquals(1, listings.size)
    }

    @Test
    fun `cancelListingCore returns the pokemon to the seller and removes the listing`() {
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "serialized_data", 100.0)
        )
        val seller = mock<LuxPlayer>()
        whenever(seller.uniqueId).thenReturn(sellerUuid)
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val realPokemon = mock<Pokemon>()
        whenever(party.getFirstAvailablePosition()).thenReturn(PartyPosition(0))
        whenever(party.add(realPokemon)).thenReturn(true)

        val result = cancelListingCore(seller, party, pc, listingId, listings, deserialize = { realPokemon })

        assertTrue(result)
        verify(party).add(realPokemon)
        assertTrue(listings.isEmpty())
    }

    @Test
    fun `cancelListingCore fails when the caller does not own the listing`() {
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "serialized_data", 100.0)
        )
        val notSeller = mock<LuxPlayer>()
        whenever(notSeller.uniqueId).thenReturn(UUID.randomUUID())
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()

        val result = cancelListingCore(notSeller, party, pc, listingId, listings, deserialize = { mock<Pokemon>() })

        assertFalse(result)
        assertEquals(1, listings.size)
    }

    @Test
    fun `cancelListingCore leaves the listing intact when grant fails`() {
        val sellerUuid = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val listings = mutableMapOf(
            listingId to TradeListing(listingId, sellerUuid, "Merchant", "serialized_data", 100.0)
        )
        val seller = mock<LuxPlayer>()
        whenever(seller.uniqueId).thenReturn(sellerUuid)
        val party = mock<PlayerPartyStore>()
        val pc = mock<PCStore>()
        val realPokemon = mock<Pokemon>()
        whenever(party.getFirstAvailablePosition()).thenReturn(null)
        whenever(pc.add(realPokemon)).thenReturn(false)

        val result = cancelListingCore(seller, party, pc, listingId, listings, deserialize = { realPokemon })

        assertFalse(result)
        assertEquals(1, listings.size)
    }
}
