package com.novaco.luxapi.cobblemon.gts

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.economy.TradeTaxManager
import com.novaco.luxapi.cobblemon.pokemon.getParty
import com.novaco.luxapi.cobblemon.serialization.PokemonSerializer
import com.novaco.luxapi.cobblemon.storage.PCStorageManager
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.economy.EconomyService
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
     * Executes the purchase of a Pokémon listing. Deserializes the real listed Pokémon and
     * verifies the buyer has room for it *before* any funds move, so a buyer is never charged
     * for a trade that can't complete. On success, the seller receives the price minus
     * [TradeTaxManager]'s computed tax, and the buyer receives the actual listed Pokémon
     * (not a blank placeholder).
     */
    fun purchaseListing(buyer: LuxPlayer, listingId: UUID): TradeResult {
        return purchaseListingCore(
            buyer, buyer.getParty(), PCStorageManager.getPC(buyer), listingId, activeListings, LuxAPI.getEconomyService()
        )
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

/**
 * Core purchase logic operating directly on the buyer's stores and the economy service,
 * independent of platform resolution so it can be unit tested without a running server. See
 * [GlobalTradeManager.purchaseListing] for the public, [LuxPlayer]-facing entry point.
 */
internal fun purchaseListingCore(
    buyer: LuxPlayer,
    party: PlayerPartyStore,
    pc: PCStore,
    listingId: UUID,
    listings: MutableMap<UUID, TradeListing>,
    economyService: EconomyService,
    deserialize: (String) -> Pokemon? = PokemonSerializer::deserializeFromBase64
): TradeResult {
    val listing = listings[listingId]
        ?: return TradeResult.Failure("This listing no longer exists or was already sold.")

    if (buyer.uniqueId == listing.sellerUuid) {
        return TradeResult.Failure("You cannot buy your own listing.")
    }

    val pokemon = deserialize(listing.pokemonBase64)
        ?: return TradeResult.Failure("This listing's data is corrupted and cannot be purchased.")

    if (party.getFirstAvailablePosition() == null && pc.getFirstAvailablePosition() == null) {
        return TradeResult.Failure("Your party and PC are both full.")
    }

    if (!economyService.hasEnough(buyer, listing.price)) {
        return TradeResult.Failure("Insufficient funds.")
    }

    if (!economyService.withdraw(buyer, listing.price)) {
        return TradeResult.Failure("Failed to process payment. Transaction aborted.")
    }

    listings.remove(listingId)

    val tax = TradeTaxManager.calculateTax(listing.sellerUuid, listing.price)
    economyService.deposit(listing.sellerUuid, listing.price - tax)

    grantPokemon(party, pc, pokemon)

    return TradeResult.Success(pokemon)
}
