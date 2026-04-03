package com.novaco.luxapi.cobblemon.serialization

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.server
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64

/**
 * The ultimate backbone engine for converting complex Cobblemon Pokémon objects
 * into safe, database-friendly strings, and vice-versa.
 * * Utilizing GZIP compression and Base64 encoding, this guarantees that
 * volatile NBT data (like EVs, IVs, moves, and hidden stats) will not be
 * corrupted during MySQL/MongoDB queries or Cross-Server Redis transmissions.
 *
 */
object PokemonSerializer {

    /**
     * Serializes a [Pokemon] object into a highly compressed Base64 string.
     * Perfect for saving into databases.
     *
     * @param pokemon The target [Pokemon] to serialize.
     * @return A safe Base64 string representation of the Pokémon.
     */
    fun serializeToBase64(pokemon: Pokemon): String {
        val tag = CompoundTag()
        val registryAccess = server()?.registryAccess()
            ?: throw java.lang.IllegalStateException("[LuxAPI] Server RegistryAccess is not available!")

        pokemon.saveToNBT(registryAccess, tag)

        val outputStream = ByteArrayOutputStream()

        NbtIo.writeCompressed(tag, outputStream)

        val bytes = outputStream.toByteArray()
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * Deserializes a Base64 string back into a fully functional [Pokemon] object.
     *
     * @param base64 The Base64 string retrieved from the database.
     * @return The reconstructed [Pokemon], or null if the string is invalid or corrupted.
     */
    fun deserializeFromBase64(base64: String): Pokemon? {
        return try {
            val registryAccess = server()?.registryAccess()
                ?: throw java.lang.IllegalStateException("[LuxAPI] Server RegistryAccess is not available!")

            val bytes = Base64.getDecoder().decode(base64)
            val inputStream = ByteArrayInputStream(bytes)

            val tag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap())

            val pokemon = Pokemon()
            pokemon.loadFromNBT(registryAccess, tag)

            pokemon
        } catch (e: Exception) {
            System.err.println("[LuxAPI] Failed to deserialize Pokémon from Base64 string.")
            e.printStackTrace()
            null
        }
    }
}