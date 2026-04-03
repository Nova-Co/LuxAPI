package com.novaco.luxapi.cobblemon.gts

import java.util.UUID

/**
 * Represents a Pokémon that is currently listed on the Global Trade System.
 */
data class TradeListing(
    val listingId: UUID = UUID.randomUUID(),
    val sellerUuid: UUID,
    val sellerName: String,
    val pokemonBase64: String,
    val price: Double,
    val createdAt: Long = System.currentTimeMillis()
)