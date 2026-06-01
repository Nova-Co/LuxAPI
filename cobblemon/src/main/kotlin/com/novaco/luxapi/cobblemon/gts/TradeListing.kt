package com.novaco.luxapi.cobblemon.gts

import java.util.UUID

/**
 * Represents a single Pokémon listing on the Global Trade System (GTS).
 * This data class holds all the necessary information for a trade, including
 * the seller's details, the Pokémon being sold, and the price.
 *
 * @property listingId A unique identifier for this specific trade listing.
 * @property sellerUuid The UUID of the player who listed the Pokémon.
 * @property sellerName The in-game name of the seller.
 * @property pokemonBase64 A Base64 encoded string representing the full data of the Pokémon.
 * @property price The asking price for the Pokémon.
 * @property createdAt The timestamp (in milliseconds) when the listing was created.
 */
data class TradeListing(
    val listingId: UUID = UUID.randomUUID(),
    val sellerUuid: UUID,
    val sellerName: String,
    val pokemonBase64: String,
    val price: Double,
    val createdAt: Long = System.currentTimeMillis()
)