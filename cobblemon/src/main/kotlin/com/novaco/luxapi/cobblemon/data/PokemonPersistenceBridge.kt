package com.novaco.luxapi.cobblemon.data

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.nbt.CompoundTag

/**
 * A safe bridge to read and write persistent metadata directly into a Pokemon.
 * Data stored using this bridge will persist across server restarts,
 * PC storage, and player trades.
 */
object PokemonPersistenceBridge {

    /**
     * Checks if the Pokemon contains a specific metadata key.
     *
     * @param pokemon The target Pokemon.
     * @param key The unique identifier to check.
     * @return True if the key exists, false otherwise.
     */
    fun hasKey(pokemon: Pokemon, key: String): Boolean {
        return pokemon.persistentData.contains(key)
    }

    /**
     * Removes a specific metadata key from the Pokemon.
     *
     * @param pokemon The target Pokemon.
     * @param key The unique identifier to remove.
     */
    fun remove(pokemon: Pokemon, key: String) {
        pokemon.persistentData.remove(key)
    }

    /**
     * Injects a String value into the Pokemon's persistent data.
     *
     * @param pokemon The target Pokemon.
     * @param key The unique identifier.
     * @param value The String value to store.
     */
    fun setString(pokemon: Pokemon, key: String, value: String) {
        pokemon.persistentData.putString(key, value)
    }

    /**
     * Retrieves a String value from the Pokemon's persistent data.
     *
     * @param pokemon The target Pokemon.
     * @param key The unique identifier.
     * @return The stored String, or null if the key does not exist.
     */
    fun getString(pokemon: Pokemon, key: String): String? {
        if (!hasKey(pokemon, key)) return null
        return pokemon.persistentData.getString(key)
    }

    /**
     * Injects an Integer value into the Pokemon's persistent data.
     *
     * @param pokemon The target Pokemon.
     * @param key The unique identifier.
     * @param value The Int value to store.
     */
    fun setInt(pokemon: Pokemon, key: String, value: Int) {
        pokemon.persistentData.putInt(key, value)
    }

    /**
     * Retrieves an Integer value from the Pokemon's persistent data.
     *
     * @param pokemon The target Pokemon.
     * @param key The unique identifier.
     * @return The stored Int, or null if the key does not exist.
     */
    fun getInt(pokemon: Pokemon, key: String): Int? {
        if (!hasKey(pokemon, key)) return null
        return pokemon.persistentData.getInt(key)
    }

    /**
     * Injects a Boolean value into the Pokemon's persistent data.
     *
     * @param pokemon The target Pokemon.
     * @param key The unique identifier.
     * @param value The Boolean value to store.
     */
    fun setBoolean(pokemon: Pokemon, key: String, value: Boolean) {
        pokemon.persistentData.putBoolean(key, value)
    }

    /**
     * Retrieves a Boolean value from the Pokemon's persistent data.
     *
     * @param pokemon The target Pokemon.
     * @param key The unique identifier.
     * @return The stored Boolean, or false as a default fallback.
     */
    fun getBoolean(pokemon: Pokemon, key: String): Boolean {
        if (!hasKey(pokemon, key)) return false
        return pokemon.persistentData.getBoolean(key)
    }
}

/**
 * Extension properties and functions for cleaner syntax.
 */

/**
 * Extension function to quickly set a String metadata tag on a Pokemon.
 * Usage: pokemon.setMetadata("event_caught", "summer_2026")
 */
fun Pokemon.setMetadata(key: String, value: String) {
    PokemonPersistenceBridge.setString(this, key, value)
}

/**
 * Extension function to quickly retrieve a String metadata tag from a Pokemon.
 * Usage: val event = pokemon.getMetadataString("event_caught")
 */
fun Pokemon.getMetadataString(key: String): String? {
    return PokemonPersistenceBridge.getString(this, key)
}

/**
 * Extension function to check if a Pokemon has a specific metadata flag.
 * Usage: if (pokemon.hasMetadata("is_starter")) { ... }
 */
fun Pokemon.hasMetadata(key: String): Boolean {
    return PokemonPersistenceBridge.hasKey(this, key)
}