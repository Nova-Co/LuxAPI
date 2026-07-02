package com.novaco.luxapi.cobblemon.data

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.serialization.toBase64String
import com.novaco.luxapi.cobblemon.serialization.toPokemon
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

/**
 * A safe bridge to read and write persistent metadata directly into a Pokemon.
 * Upgraded with a high-performance Asynchronous Pipeline to prevent main thread ticks lagging.
 */
object PokemonPersistenceBridge {

    // Dedicated Thread-safe Containment Layer for Background Workload Processing
    private val asyncPool = ForkJoinPool.commonPool()

    fun hasKey(pokemon: Pokemon, key: String): Boolean = pokemon.persistentData.contains(key)
    fun remove(pokemon: Pokemon, key: String) { pokemon.persistentData.remove(key) }
    fun setString(pokemon: Pokemon, key: String, value: String) { pokemon.persistentData.putString(key, value) }
    fun getString(pokemon: Pokemon, key: String): String? = if (!hasKey(pokemon, key)) null else pokemon.persistentData.getString(key)
    fun setInt(pokemon: Pokemon, key: String, value: Int) { pokemon.persistentData.putInt(key, value) }
    fun getInt(pokemon: Pokemon, key: String): Int? = if (!hasKey(pokemon, key)) null else pokemon.persistentData.getInt(key)
    fun setBoolean(pokemon: Pokemon, key: String, value: Boolean) { pokemon.persistentData.putBoolean(key, value) }
    fun getBoolean(pokemon: Pokemon, key: String): Boolean = if (!hasKey(pokemon, key)) false else pokemon.persistentData.getBoolean(key)

    /**
     * Asynchronously serializes and commits a Pokemon instance to SQL/NoSQL storage providers.
     * Leverages the dynamic LuxAPI AttributeManager system at background priority.
     *
     * @param playerUuid The entity owner of this specific Pokemon data packet.
     * @param pokemon The targeting Pokemon asset to freeze.
     * @return A promise containing the execution success flag.
     */
    fun saveToDatabaseAsync(playerUuid: UUID, pokemon: UUID, pokemonInstance: Pokemon): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            try {
                // 1. Process encoding & compression workload safely at background
                val encryptedPayload = pokemonInstance.toBase64String()

                // 2. Transmit string packet over via the unified AttributeManager system
                // Fallback check to prevent crash if AttributeManager initialization isn't loaded
                val attributeManagerClass = Class.forName("com.novaco.luxapi.database.AttributeManager")
                val saveMethod = attributeManagerClass.getMethod("setPokemonData", UUID::class.java, UUID::class.java, String::class.java)
                saveMethod.invoke(null, playerUuid, pokemon, encryptedPayload)

                true
            } catch (cnfe: ClassNotFoundException) {
                println("[LuxAPI | Persistence Alert] Core database module missing. Skipping async database flush.")
                false
            } catch (t: Throwable) {
                println("[LuxAPI | Critical Error] Dynamic thread pool capture failed storage update pipeline: ${t.message}")
                false
            }
        }, asyncPool)
    }

    /**
     * Asynchronously reads and pulls a Base64 payload block from the database layer,
     * compiling it back into a valid live state instance.
     *
     * @param playerUuid The entity owner target.
     * @param pokemon The static UUID identifier node assigned to the target data frame.
     * @return A promise containing the reconstructed Pokemon reference, or null.
     */
    fun loadFromDatabaseAsync(playerUuid: UUID, pokemon: UUID): CompletableFuture<Pokemon?> {
        return CompletableFuture.supplyAsync({
            try {
                val attributeManagerClass = Class.forName("com.novaco.luxapi.database.AttributeManager")
                val getMethod = attributeManagerClass.getMethod("getPokemonData", UUID::class.java, UUID::class.java)
                val stringPayload = getMethod.invoke(null, playerUuid, pokemon) as? String ?: return@supplyAsync null

                // Perform heavy decompression decoding loop off-thread
                stringPayload.toPokemon()
            } catch (e: Exception) {
                println("[LuxAPI] Could not safely extract and thread load requested Pokemon index: ${e.message}")
                null
            }
        }, asyncPool)
    }
}

fun Pokemon.setMetadata(key: String, value: String) { PokemonPersistenceBridge.setString(this, key, value) }
fun Pokemon.getMetadataString(key: String): String? = PokemonPersistenceBridge.getString(this, key)
fun Pokemon.hasMetadata(key: String): Boolean = PokemonPersistenceBridge.hasKey(this, key)