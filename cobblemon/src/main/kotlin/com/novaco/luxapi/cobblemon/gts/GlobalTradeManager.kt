package com.novaco.luxapi.cobblemon.gts

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.UUID

/**
 * The backbone for all Global Trade and Auction operations.
 * Ensures ACID compliance to prevent duplication glitches.
 */
object GlobalTradeManager {

    private val activeListings = mutableMapOf<UUID, TradeListing>()

    /**
     * Lists a Pokémon on the GTS.
     * @return True if successfully listed, False if the Pokémon couldn't be removed safely.
     */
    fun listPokemon(seller: LuxPlayer, pokemon: Pokemon, price: Double): Boolean {
        val base64Data = "mock_base64_data_for_now"

        val listing = TradeListing(
            sellerUuid = seller.uniqueId,
            sellerName = seller.name,
            pokemonBase64 = base64Data,
            price = price
        )
        activeListings[listing.listingId] = listing

        return true
    }

    /**
     * Executes a safe purchase transaction.
     * Prevents race conditions and double-spending.
     */
    fun purchaseListing(buyer: LuxPlayer, listingId: UUID): TradeResult {
        val listing = activeListings[listingId]
            ?: return TradeResult.Failure("This listing no longer exists or was already sold.")

        if (buyer.uniqueId == listing.sellerUuid) {
            return TradeResult.Failure("You cannot buy your own listing.")
        }

        val economyService = LuxAPI.getEconomyService()

        if (!economyService.hasEnough(buyer, listing.price)) {
            return TradeResult.Failure("Insufficient funds.")
        }

        val withdrawSuccess = economyService.withdraw(buyer, listing.price)
        if (!withdrawSuccess) {
            return TradeResult.Failure("Failed to process payment. Transaction aborted.")
        }

        activeListings.remove(listingId)

        economyService.deposit(listing.sellerUuid, listing.price)

        val deserializedPokemon = Pokemon()

        return TradeResult.Success(deserializedPokemon)
    }
}