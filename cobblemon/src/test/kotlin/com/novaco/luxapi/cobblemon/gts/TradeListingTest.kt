package com.novaco.luxapi.cobblemon.gts

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class TradeListingTest {

    @Test
    fun `test default parameters generation`() {
        val sellerId = UUID.randomUUID()
        val beforeCreation = System.currentTimeMillis()

        val listing = TradeListing(
            sellerUuid = sellerId,
            sellerName = "Merchant",
            pokemonBase64 = "base64_string",
            price = 150.0
        )

        val afterCreation = System.currentTimeMillis()

        assertNotNull(listing.listingId)
        assertEquals(sellerId, listing.sellerUuid)
        assertEquals("Merchant", listing.sellerName)
        assertEquals("base64_string", listing.pokemonBase64)
        assertEquals(150.0, listing.price)
        assertTrue(listing.createdAt in beforeCreation..afterCreation)
    }
}