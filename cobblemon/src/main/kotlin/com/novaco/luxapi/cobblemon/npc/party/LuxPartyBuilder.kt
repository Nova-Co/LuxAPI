package com.novaco.luxapi.cobblemon.npc.party

import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.npc.partyproviders.PoolPartyProvider
import com.cobblemon.mod.common.api.npc.partyproviders.SimplePartyProvider
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * A fluent API builder for configuring native Cobblemon NPCPartyProviders.
 * Fully compatible with MoLang Expression systems introduced in newer Cobblemon iterations.
 */
class LuxPartyBuilder {

    private val staticSpecs = mutableListOf<String>()
    private val poolEntries = mutableListOf<PoolEntryData>()

    private var poolMinCount = "1"
    private var poolMaxCount = "6"
    private var useFixedRandom = false

    /**
     * Inner transfer block defining structural layouts for a dynamic pool member.
     */
    private data class PoolEntryData(
        val spec: String,
        val weight: String,
        val levelVariation: String,
        val selectableTimes: String,
        val npcLevels: String
    )

    /**
     * appends a definitive static Pokémon specification into the non-randomized array slot.
     *
     * @param spec The explicit property designator (e.g., "charizard level=50 shiny=true").
     * @return The updated builder context instance.
     */
    fun add(spec: String): LuxPartyBuilder {
        if (staticSpecs.size < 6) {
            staticSpecs.add(spec)
        }
        return this
    }

    /**
     * Defines size boundaries for dynamic selector engines using strict raw values or MoLang expressions.
     *
     * @param min The text formula defining lower capacity bounds.
     * @param max The text formula defining upper capacity bounds.
     * @return The updated builder context instance.
     */
    fun randomizeFromPool(min: String, max: String): LuxPartyBuilder {
        this.poolMinCount = min
        this.poolMaxCount = max
        return this
    }

    /**
     * Defines size boundaries for dynamic selector engines using discrete size boundaries.
     *
     * @param min The exact minimum party allocation count.
     * @param max The exact maximum party allocation count.
     * @return The updated builder context instance.
     */
    fun randomizeFromPool(min: Int, max: Int): LuxPartyBuilder {
        val clampedMin = min.coerceIn(1, 6)
        val clampedMax = max.coerceIn(clampedMin, 6)
        this.poolMinCount = clampedMin.toString()
        this.poolMaxCount = clampedMax.toString()
        return this
    }

    /**
     * Appends a complex contextual specification node entry directly into the randomized selection table.
     *
     * @param spec The concrete property designator mapping target traits.
     * @param weight The chance calculation formula or value.
     * @param levelVariation The level alteration offset bound formula or value.
     * @param selectableTimes The extraction maximum capacity constraint formula or value.
     * @param npcLevels The validity limit format boundary string (e.g., "1-100").
     * @return The updated builder context instance.
     */
    @JvmOverloads
    fun addPoolEntry(
        spec: String,
        weight: String = "1",
        levelVariation: String = "0",
        selectableTimes: String = "1",
        npcLevels: String = "1-100"
    ): LuxPartyBuilder {
        poolEntries.add(PoolEntryData(spec, weight, levelVariation, selectableTimes, npcLevels))
        return this
    }

    /**
     * Controls whether the stochastic generation mechanics should lock onto specific stable seeds.
     *
     * @param fixed State flag denoting deterministic mapping constraint.
     * @return The updated builder context instance.
     */
    fun fixedRandom(fixed: Boolean = true): LuxPartyBuilder {
        this.useFixedRandom = fixed
        return this
    }

    /**
     * Transforms the current builder instance layout structures into verified native platform party nodes.
     *
     * @return The verified instance implementation node, or null if configurations are empty.
     */
    internal fun build(): NPCPartyProvider? {
        if (poolEntries.isNotEmpty()) {
            val provider = PoolPartyProvider()
            val json = JsonObject()

            json.addProperty("isStatic", false)
            json.addProperty("useFixedRandom", this.useFixedRandom)
            json.addProperty("minPokemon", this.poolMinCount)
            json.addProperty("maxPokemon", this.poolMaxCount)

            val poolArray = JsonArray()
            for (entry in poolEntries) {
                val entryJson = JsonObject()
                entryJson.addProperty("pokemon", entry.spec)
                entryJson.addProperty("weight", entry.weight)
                entryJson.addProperty("levelVariation", entry.levelVariation)
                entryJson.addProperty("selectableTimes", entry.selectableTimes)
                entryJson.addProperty("npcLevels", entry.npcLevels)
                poolArray.add(entryJson)
            }
            json.add("pool", poolArray)

            provider.loadFromJSON(json)
            return provider
        }

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