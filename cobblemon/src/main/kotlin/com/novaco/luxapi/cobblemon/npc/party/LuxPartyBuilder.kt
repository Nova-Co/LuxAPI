package com.novaco.luxapi.cobblemon.npc.party

import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.npc.partyproviders.PoolPartyProvider
import com.cobblemon.mod.common.api.npc.partyproviders.SimplePartyProvider
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * A fluent API for developers to easily construct NPC teams.
 * Supports both static parties and randomized pool-based parties.
 */
class LuxPartyBuilder {

    private val staticSpecs = mutableListOf<String>()
    private val poolEntries = mutableListOf<PoolEntryData>()

    private var poolMinCount = 1
    private var poolMaxCount = 6
    private var useFixedRandom = false

    private data class PoolEntryData(
        val spec: String,
        val weight: Float,
        val levelVariation: Int,
        val selectableTimes: Int,
        val npcLevels: IntRange
    )

    /**
     * Adds a static Pokémon to the team via spec string.
     */
    fun add(spec: String): LuxPartyBuilder {
        if (staticSpecs.size < 6) {
            staticSpecs.add(spec)
        }
        return this
    }

    /**
     * Configures the party to act as a randomized pool.
     */
    fun randomizeFromPool(min: Int, max: Int): LuxPartyBuilder {
        this.poolMinCount = min.coerceIn(1, 6)
        this.poolMaxCount = max.coerceIn(this.poolMinCount, 6)
        return this
    }

    /**
     * Adds an entry to the randomized Pokémon pool.
     */
    @JvmOverloads
    fun addPoolEntry(
        spec: String,
        weight: Float = 1.0f,
        levelVariation: Int = 0,
        selectableTimes: Int = 1,
        npcLevels: IntRange = 1..100
    ): LuxPartyBuilder {
        poolEntries.add(PoolEntryData(spec, weight, levelVariation, selectableTimes, npcLevels))
        return this
    }

    /**
     * Sets whether the random generation should use a fixed seed based on NPC UUID.
     */
    fun fixedRandom(fixed: Boolean = true): LuxPartyBuilder {
        this.useFixedRandom = fixed
        return this
    }

    /**
     * Compiles the configuration into a native Cobblemon NPCPartyProvider.
     */
    internal fun build(): NPCPartyProvider? {
        // Handle PoolPartyProvider via JSON bypass
        if (poolEntries.isNotEmpty()) {
            val provider = PoolPartyProvider()
            val json = JsonObject()
            json.addProperty("isStatic", false)
            json.addProperty("useFixedRandom", this.useFixedRandom)
            json.addProperty("minPokemon", this.poolMinCount.toString())
            json.addProperty("maxPokemon", this.poolMaxCount.toString())

            val poolArray = JsonArray()
            for (entry in poolEntries) {
                val entryJson = JsonObject()
                entryJson.addProperty("pokemon", entry.spec)
                entryJson.addProperty("weight", entry.weight.toString())
                entryJson.addProperty("levelVariation", entry.levelVariation.toString())
                entryJson.addProperty("selectableTimes", entry.selectableTimes.toString())
                entryJson.addProperty("npcLevels", "${entry.npcLevels.first}-${entry.npcLevels.last}")
                poolArray.add(entryJson)
            }
            json.add("pool", poolArray)
            provider.loadFromJSON(json)
            return provider
        }

        // Handle SimplePartyProvider via JSON bypass to avoid type mismatch and MoLang issues
        if (staticSpecs.isNotEmpty()) {
            val provider = SimplePartyProvider()
            val json = JsonObject()
            val pokemonArray = JsonArray()
            staticSpecs.forEach { pokemonArray.add(it) }
            json.add("pokemon", pokemonArray)

            provider.loadFromJSON(json)
            return provider
        }

        return null
    }
}