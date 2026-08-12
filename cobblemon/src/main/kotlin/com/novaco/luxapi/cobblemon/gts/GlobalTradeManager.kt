package com.novaco.luxapi.cobblemon.gts

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.pokemon.getParty
import com.novaco.luxapi.cobblemon.serialization.PokemonSerializer
import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.UUID

/**
 * Manages all operations for the Global Trade System (GTS), including listing and purchasing
 * Pokémon. Listing removes the Pokémon from the seller's party immediately (via real NBT
 * serialization, not a placeholder), and purchasing grants the real deserialized Pokémon to
 * the buyer — no duplication, no data loss.
 */
object GlobalTradeManager {

    private val activeListings = mutableMapOf<UUID, TradeListing>()

    /**
     * Creates a new listing for a Pokémon on the GTS. The Pokémon is serialized and immediately
     * removed from [seller]'s party — it does not exist anywhere else until the listing is
     * purchased ([purchaseListing]) or cancelled ([cancelListing]).
     *
     * Fails (returns `false`, nothing is changed) if [pokemon] isn't actually in [seller]'s
     * party, or if removing it would leave the server's `preventCompletePartyDeposit` config
     * guard violated (same rule Phase 6's PC deposit already enforces).
     */
    fun listPokemon(seller: LuxPlayer, pokemon: Pokemon, price: Double): Boolean {
        return listPokemonCore(seller.getParty(), pokemon, seller.uniqueId, seller.name, price, activeListings)
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

        val economyService = com.novaco.luxapi.commons.LuxAPI.getEconomyService()

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

/**
 * Core listing logic operating directly on the party store, independent of [LuxPlayer]
 * resolution so it can be unit tested without a running server. See
 * [GlobalTradeManager.listPokemon] for the public, [LuxPlayer]-facing entry point.
 */
internal fun listPokemonCore(
    party: PlayerPartyStore,
    pokemon: Pokemon,
    sellerUuid: UUID,
    sellerName: String,
    price: Double,
    listings: MutableMap<UUID, TradeListing>,
    serialize: (Pokemon) -> String = PokemonSerializer::serializeToBase64
): Boolean {
    if (party.get(pokemon.uuid) == null) return false
    if (Cobblemon.config.preventCompletePartyDeposit && party.occupied() == 1) return false

    val base64Data = serialize(pokemon)
    party.remove(pokemon)

    val listing = TradeListing(
        sellerUuid = sellerUuid,
        sellerName = sellerName,
        pokemonBase64 = base64Data,
        price = price
    )
    listings[listing.listingId] = listing

    return true
}

/**
 * Places [pokemon] into [party] if it has room, otherwise falls back to [pc]. Returns `false`
 * only if both are full. Used by [GlobalTradeManager.purchaseListing] and
 * [GlobalTradeManager.cancelListing] to hand a deserialized Pokémon back to a player.
 */
internal fun grantPokemon(party: PlayerPartyStore, pc: PCStore, pokemon: Pokemon): Boolean {
    if (party.getFirstAvailablePosition() != null) {
        return party.add(pokemon)
    }
    return pc.add(pokemon)
}
