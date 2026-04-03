package com.novaco.luxapi.cobblemon.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

/**
 * Extension functions providing a high-level bridge between LuxAPI and Cobblemon.
 * These utilities allow developers to interact with Pokémon data directly
 * through the LuxPlayer object without deep-diving into Cobblemon's storage internals.
 *
 * @author NovaCo
 */

/**
 * Accesses the player's current Pokémon party.
 *
 * @return The PlayerPartyStore instance for the player.
 */
fun LuxPlayer.getParty(): PlayerPartyStore {
    return Cobblemon.storage.getParty(this.parent as ServerPlayer)
}

/**
 * Accesses the player's personal computer (PC) storage.
 *
 * @return The PCStore containing all Pokémon boxes for the player.
 */
fun LuxPlayer.getPC(): PCStore {
    return Cobblemon.storage.getPC(this.parent as ServerPlayer)
}

/**
 * Checks if the player's party contains at least one Pokémon of the specified species.
 *
 * @param speciesName The name of the Pokémon species (e.g., "charizard").
 * @return True if present, false otherwise.
 */
fun LuxPlayer.hasInParty(speciesName: String): Boolean {
    return this.getParty().any { it.species.name.equals(speciesName, ignoreCase = true) }
}

/**
 * Retrieves a Pokémon from a specific party slot.
 *
 * @param slot The slot index (0 to 5).
 * @return The Pokémon in that slot, or null if empty.
 */
fun LuxPlayer.getPokemonInSlot(slot: Int): Pokemon? {
    if (slot !in 0..5) return null
    return this.getParty().get(slot)
}

/**
 * Calculates the Individual Value (IV) percentage of a Pokémon.
 * A "Perfect" Pokémon (31 in all 6 stats) results in 100.0%.
 *
 * @return The percentage value (0.0 - 100.0).
 */
fun Pokemon.getIVPercentage(): Double {
    val totalIv = Stats.values().sumOf { stat -> (this.ivs.get(stat) ?: 0).toDouble() }
    val maxPossibleIv = 31.0 * 6
    return (totalIv / maxPossibleIv) * 100.0
}

/**
 * Determines if a Pokémon is "Battle Ready" by checking its level and IV perfection.
 *
 * @return True if Level 100 and all IVs are 31.
 */
fun Pokemon.isPerfect(): Boolean {
    val hasMaxIvs = Stats.values().all { stat -> (this.ivs.get(stat) ?: 0) >= 31 }
    return this.level >= 100 && hasMaxIvs
}

/**
 * Quickly fetches the integer value of a specific stat.
 *
 * @param stat The target stat (e.g., Stats.SPEED).
 * @return The current value of that stat.
 */
fun Pokemon.getStatValue(stat: Stats): Int {
    return this.getStat(stat)
}