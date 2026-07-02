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
 */
object PokemonSerializer {

    /**
     * Serializes a [Pokemon] object into a highly compressed Base64 string.
     * Throws explicit errors if server registry state is detached.
     */
    fun serializeToBase64(pokemon: Pokemon): String {
        val tag = CompoundTag()
        val registryAccess = server()?.registryAccess()
            ?: throw java.lang.IllegalStateException("[LuxAPI] Server RegistryAccess is missing during serialization flow!")

        pokemon.saveToNBT(registryAccess, tag)

        val outputStream = ByteArrayOutputStream()
        NbtIo.writeCompressed(tag, outputStream)

        val bytes = outputStream.toByteArray()
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * Deserializes a Base64 string back into a fully functional [Pokemon] object.
     */
    fun deserializeFromBase64(base64: String): Pokemon? {
        return try {
            val registryAccess = server()?.registryAccess()
                ?: throw java.lang.IllegalStateException("[LuxAPI] Server RegistryAccess is missing during deserialization flow!")

            val bytes = Base64.getDecoder().decode(base64)
            val inputStream = ByteArrayInputStream(bytes)

            val tag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap())

            val pokemon = Pokemon()
            pokemon.loadFromNBT(registryAccess, tag)

            pokemon
        } catch (e: Exception) {
            System.err.println("[LuxAPI | Core Engine Error] Failed to reconstruct Pokemon from payload array.")
            e.printStackTrace()
            null
        }
    }
}