package com.novaco.luxapi.cobblemon.manager

import com.novaco.luxapi.cobblemon.pokemon.getPC
import com.novaco.luxapi.cobblemon.pokemon.getParty
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Extension functions providing high-level management for a player's Pokémon party
 * and PC storage. Designed for rapid development of gameplay mechanics.
 *
 * @author NovaCo
 */

/**
 * Instantly heals all Pokémon currently in the player's party.
 * Revives fainted Pokémon and fully restores HP and PP.
 */
fun LuxPlayer.healParty() {
    this.getParty().heal()
}

/**
 * Safely transfers a Pokémon from the player's active party to their PC box.
 * The function verifies if the target slot contains a Pokémon and ensures
 * the PC has enough space to accept it.
 *
 * @param partySlot The index of the party slot (0 to 5) to transfer.
 * @return True if the transfer was successful, false if the slot was empty or transfer failed.
 */
fun LuxPlayer.transferToPC(partySlot: Int): Boolean {
    if (partySlot !in 0..5) return false

    val party = this.getParty()
    val targetPokemon = party.get(partySlot) ?: return false
    val pc = this.getPC()
    val successfullyAdded = pc.add(targetPokemon)

    if (successfullyAdded) {
        party.remove(targetPokemon)
        return true
    }

    return false
}

/**
 * Retrieves the count of currently conscious (not fainted) Pokémon in the party.
 * Useful for validating if a player can enter a battle or specific zones.
 *
 * @return The number of alive Pokémon.
 */
fun LuxPlayer.getAlivePokemonCount(): Int {
    return this.getParty().count { it.currentHealth > 0 }
}