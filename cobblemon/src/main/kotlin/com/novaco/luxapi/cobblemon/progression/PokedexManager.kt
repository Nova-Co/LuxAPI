package com.novaco.luxapi.cobblemon.progression

import com.cobblemon.mod.common.api.pokedex.AbstractPokedexManager
import com.cobblemon.mod.common.api.pokedex.CaughtCount
import com.cobblemon.mod.common.api.pokedex.CaughtPercent
import com.cobblemon.mod.common.api.pokedex.Dexes
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokedex.SeenCount
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.util.pokedex
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * A centralized manager for handling Pokédex-related progression. Wraps Cobblemon's real
 * per-species knowledge tracking ([PokedexEntryProgress], not string-matched) and its built-in
 * dex-relative/global calculators — no hardcoded species totals.
 *
 * Every function optionally accepts a [ResourceLocation] `dexId` (one of [getAvailableDexes])
 * to scope the query to a specific regional dex (e.g. `cobblemon:kanto`). Omitting it (or
 * passing `null`) queries across every species Cobblemon knows about.
 */
object PokedexManager {

    /**
     * Whether the player has actually caught (not just seen) [species].
     */
    fun hasCaught(player: LuxPlayer, species: String): Boolean {
        val speciesId = resolveSpeciesId(species) ?: return false
        return hasCaughtCore((player.parent as ServerPlayer).pokedex(), speciesId)
    }

    /**
     * Whether the player has encountered or caught [species] (any knowledge beyond none).
     */
    fun hasSeen(player: LuxPlayer, species: String): Boolean {
        val speciesId = resolveSpeciesId(species) ?: return false
        return hasSeenCore((player.parent as ServerPlayer).pokedex(), speciesId)
    }

    /**
     * The player's real knowledge level for [species]. Returns [PokedexEntryProgress.NONE] for
     * an unresolvable species name.
     */
    fun getKnowledgeForSpecies(player: LuxPlayer, species: String): PokedexEntryProgress {
        val speciesId = resolveSpeciesId(species) ?: return PokedexEntryProgress.NONE
        return (player.parent as ServerPlayer).pokedex().getKnowledgeForSpecies(speciesId)
    }

    /**
     * Number of unique species the player has caught, scoped to [dexId] if given, otherwise
     * counted across every species Cobblemon knows about.
     */
    fun getCaughtCount(player: LuxPlayer, dexId: ResourceLocation? = null): Int {
        return getCaughtCountCore((player.parent as ServerPlayer).pokedex(), dexId)
    }

    /**
     * Number of unique species the player has encountered or caught, scoped to [dexId] if
     * given, otherwise counted across every species Cobblemon knows about.
     */
    fun getSeenCount(player: LuxPlayer, dexId: ResourceLocation? = null): Int {
        return getSeenCountCore((player.parent as ServerPlayer).pokedex(), dexId)
    }

    /**
     * The player's real catch-completion percentage (`0.0`-`100.0`), scoped to [dexId] if
     * given, otherwise measured against every species Cobblemon knows about. No hardcoded
     * species total — this reads Cobblemon's own real entry count.
     */
    fun getCompletionPercentage(player: LuxPlayer, dexId: ResourceLocation? = null): Double {
        return getCompletionPercentageCore((player.parent as ServerPlayer).pokedex(), dexId)
    }

    /**
     * Every dex ID currently registered (e.g. `cobblemon:kanto`, `cobblemon:national`), usable
     * as the `dexId` argument to every other function on this object.
     */
    fun getAvailableDexes(): List<ResourceLocation> {
        return Dexes.dexEntryMap.keys.toList()
    }

    private fun resolveSpeciesId(species: String): ResourceLocation? {
        return PokemonSpecies.getByName(species.lowercase())?.resourceIdentifier
    }
}

/**
 * Core knowledge-comparison logic operating directly on a resolved [AbstractPokedexManager],
 * independent of [LuxPlayer]/[ServerPlayer] resolution so it's unit-testable without a running
 * server. See [PokedexManager.hasCaught] for the public, [LuxPlayer]-facing entry point.
 */
internal fun hasCaughtCore(dexManager: AbstractPokedexManager, speciesId: ResourceLocation): Boolean {
    return dexManager.getKnowledgeForSpecies(speciesId) == PokedexEntryProgress.CAUGHT
}

/**
 * See [hasCaughtCore]; backs [PokedexManager.hasSeen].
 */
internal fun hasSeenCore(dexManager: AbstractPokedexManager, speciesId: ResourceLocation): Boolean {
    return dexManager.getKnowledgeForSpecies(speciesId) != PokedexEntryProgress.NONE
}

/**
 * Delegates to Cobblemon's own [CaughtCount] calculator — global scope when [dexId] is `null`,
 * dex-scoped otherwise. Backs [PokedexManager.getCaughtCount].
 */
internal fun getCaughtCountCore(dexManager: AbstractPokedexManager, dexId: ResourceLocation?): Int {
    return if (dexId == null) {
        dexManager.getGlobalCalculatedValue(CaughtCount)
    } else {
        dexManager.getDexCalculatedValue(dexId, CaughtCount)
    }
}

/**
 * Delegates to Cobblemon's own [SeenCount] calculator — global scope when [dexId] is `null`,
 * dex-scoped otherwise. Backs [PokedexManager.getSeenCount].
 */
internal fun getSeenCountCore(dexManager: AbstractPokedexManager, dexId: ResourceLocation?): Int {
    return if (dexId == null) {
        dexManager.getGlobalCalculatedValue(SeenCount)
    } else {
        dexManager.getDexCalculatedValue(dexId, SeenCount)
    }
}

/**
 * Delegates to Cobblemon's own [CaughtPercent] calculator (widened from `Float` to `Double`) —
 * global scope when [dexId] is `null`, dex-scoped otherwise. Backs
 * [PokedexManager.getCompletionPercentage].
 */
internal fun getCompletionPercentageCore(dexManager: AbstractPokedexManager, dexId: ResourceLocation?): Double {
    val percent = if (dexId == null) {
        dexManager.getGlobalCalculatedValue(CaughtPercent)
    } else {
        dexManager.getDexCalculatedValue(dexId, CaughtPercent)
    }
    return percent.toDouble()
}
