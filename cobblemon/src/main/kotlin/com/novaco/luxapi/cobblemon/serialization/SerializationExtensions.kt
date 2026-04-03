package com.novaco.luxapi.cobblemon.serialization

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Extension functions providing rapid access to the [PokemonSerializer] engine.
 * Designed to keep database integration logic extremely clean and concise.
 *
 */

/**
 * Converts this Pokémon into a Base64 encoded string safe for database storage.
 * * Usage: val dbString = myPokemon.toBase64String()
 *
 * @return A compressed Base64 string.
 */
fun Pokemon.toBase64String(): String {
    return PokemonSerializer.serializeToBase64(this)
}

/**
 * Attempts to parse this Base64 string back into a Pokémon object.
 * * Usage: val myPokemon = dbString.toPokemon()
 *
 * @return A [Pokemon] instance, or null if deserialization fails.
 */
fun String.toPokemon(): Pokemon? {
    return PokemonSerializer.deserializeFromBase64(this)
}