package com.novaco.luxapi.cobblemon.npc.party

import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.npc.partyproviders.PoolPartyProvider
import com.cobblemon.mod.common.api.npc.partyproviders.SimplePartyProvider
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * A fluent builder for constructing NPC Pokémon parties.
 * This class simplifies the creation of both fixed, static teams and complex, randomized teams
 * drawn from a weighted pool of possible Pokémon. It is used as a component within the [LuxNPCBuilder].
 */
class LuxPartyBuilder {

    private val staticSpecs = mutableListOf<String>()
    private val poolEntries = mutableListOf<PoolEntryData>()

    private var poolMinCount = 1
    private var poolMaxCount = 6
    private var useFixedRandom = false

    /** Internal data class to hold configuration for a single entry in a randomized pool. */
    private data class PoolEntryData(
        val spec: String,
        val weight: Float,
        val levelVariation: Int,
        val selectableTimes: Int,
        val npcLevels: IntRange
    )

    /**
     * Adds a Pokémon with a fixed definition to the NPC's party.
     * Use this for creating a static, non-randomized team.
     *
     * @param spec A string defining the Pokémon (e.g., "pikachu level=10 shiny").
     * @return This [LuxPartyBuilder] instance for method chaining.
     */
    fun add(spec: String): LuxPartyBuilder {
        if (staticSpecs.size < 6) {
            staticSpecs.add(spec)
        }
        return this
    }

    /**
     * Configures the party to be a randomized pool, where the final team size
     * will be between the specified min and max values.
     *
     * @param min The minimum number of Pokémon in the generated party.
     * @param max The maximum number of Pokémon in the generated party.
     * @return This [LuxPartyBuilder] instance for method chaining.
     */
    fun randomizeFromPool(min: Int, max: Int): LuxPartyBuilder {
        this.poolMinCount = min.coerceIn(1, 6)
        this.poolMaxCount = max.coerceIn(this.poolMinCount, 6)
        return this
    }

    /**
     * Adds a potential Pokémon to the randomized pool.
     *
     * @param spec A string defining the Pokémon (e.g., "snorlax").
     * @param weight The selection weight. Higher values increase the chance of this entry being chosen.
     * @param levelVariation A random value to add or subtract from the NPC's base level.
     * @param selectableTimes The maximum number of times this specific entry can be chosen for the party.
     * @param npcLevels The range of NPC levels for which this entry is valid.
     * @return This [LuxPartyBuilder] instance for method chaining.
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
     * If enabled, the random party generation will be deterministic, using the NPC's UUID as a seed.
     * This means a specific NPC will always generate the same "random" party.
     *
     * @param fixed `true` to enable deterministic generation, `false` for pure randomness.
     * @return This [LuxPartyBuilder] instance for method chaining.
     */
    fun fixedRandom(fixed: Boolean = true): LuxPartyBuilder {
        this.useFixedRandom = fixed
        return this
    }

    /**
     * Internal method to compile the builder's configuration into a native Cobblemon [NPCPartyProvider].
     * It determines whether to create a [SimplePartyProvider] (for static teams) or a
     * [PoolPartyProvider] (for randomized teams) based on the methods called.
     *
     * @return The appropriate [NPCPartyProvider], or null if no Pokémon were defined.
     */
    internal fun build(): NPCPartyProvider? {
        // If pool entries exist, build a PoolPartyProvider.
        // This uses a JSON bypass to correctly configure the provider without direct class manipulation.
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

        // If only static specs exist, build a SimplePartyProvider.
        // This also uses a JSON bypass to avoid potential type mismatches and MoLang parsing issues.
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