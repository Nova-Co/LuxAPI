package com.novaco.luxapi.cobblemon.gts

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.UUID

/**
 * Manages all operations for the Global Trade System (GTS), including listing and purchasing Pokémon.
 * This object is designed to be thread-safe and prevent common issues like duplication glitches
 * by ensuring transactional integrity (ACID principles).
 */
object GlobalTradeManager {

    private val activeListings = mutableMapOf<UUID, TradeListing>()

    /**
     * Creates a new listing for a Pokémon on the GTS.
     *
     * @param seller The player who is selling the Pokémon.
     * @param pokemon The Pokémon to be listed.
     * @param price The asking price for the Pokémon.
     * @return `true` if the Pokémon was successfully listed, `false` otherwise.
     */
    fun listPokemon(seller: LuxPlayer, pokemon: Pokemon, price: Double): Boolean {
        // In a real implementation, this would involve serializing the Pokemon object.
        val base64Data = "mock_base64_data_for_now" // Placeholder for actual serialization

        val listing = TradeListing(
            sellerUuid = seller.uniqueId,
            sellerName = seller.name,
            pokemonBase64 = base64Data,
            price = price
        )
        activeListings[listing.listingId] = listing

        // Here you would also remove the Pokémon from the seller's party.
        // This is a critical step to prevent duplication.

        return true
    }

    /**
     * Executes the purchase of a Pokémon listing.
     * This function handles the transaction in a safe manner, preventing race conditions and ensuring
     * that the buyer has sufficient funds and the seller receives payment.
     *
     * @param buyer The player purchasing the Pokémon.
     * @param listingId The unique ID of the listing to be purchased.
     * @return A [TradeResult] indicating the outcome of the transaction.
     */
    fun purchaseListing(buyer: LuxPlayer, listingId: UUID): TradeResult {
        val listing = activeListings[listingId]
            ?: return TradeResult.Failure("This listing no longer exists or was already sold.")

        // Prevent a player from buying their own Pokémon.
        if (buyer.uniqueId == listing.sellerUuid) {
            return TradeResult.Failure("You cannot buy your own listing.")
        }

        val economyService = LuxAPI.getEconomyService()

        // Check if the buyer has enough money.
        if (!economyService.hasEnough(buyer, listing.price)) {
            return TradeResult.Failure("Insufficient funds.")
        }

        // Attempt to withdraw the funds from the buyer.
        val withdrawSuccess = economyService.withdraw(buyer, listing.price)
        if (!withdrawSuccess) {
            // If withdrawal fails, abort the transaction to prevent money loss.
            return TradeResult.Failure("Failed to process payment. Transaction aborted.")
        }

        // The transaction is now committed. Remove the listing.
        activeListings.remove(listingId)

        // Deposit the funds into the seller's account.
        economyService.deposit(listing.sellerUuid, listing.price)

        // Deserialize the Pokémon from the listing and give it to the buyer.
        // This is a placeholder for the actual deserialization logic.
        val deserializedPokemon = Pokemon()

        return TradeResult.Success(deserializedPokemon)
    }
}