package com.novaco.luxapi.cobblemon.tms

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.tms.TechnicalMachine
import com.cobblemon.mod.common.api.tms.TechnicalMachines
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Query wrapper around Cobblemon's [TechnicalMachines] registry, plus per-player
 * learn/unlearn mutation via [Cobblemon.playerDataManager]'s TM store.
 *
 * **Scope note:** [TechnicalMachines] is a JSON-loaded registry with no public
 * registration entry point (data loads from datapack `tms/` resources only) — this
 * wrapper is query + per-player learn/unlearn/query only, the same shape
 * [com.novaco.luxapi.cobblemon.mark.MarkManager] uses for its own JSON-loaded registry.
 */
object TMManager {

    fun all(): Collection<TechnicalMachine> = TechnicalMachines.tmMap.values

    fun get(identifier: ResourceLocation): TechnicalMachine? = TechnicalMachines.tmMap[identifier]

    /** Finds the [TechnicalMachine] that teaches [move], via Cobblemon's own move-to-TM index. */
    fun getByMove(move: MoveTemplate): TechnicalMachine? = TechnicalMachines.moveToTM[move]

    /** Every TM [pokemon] can currently learn, resolved via its accessible/level-up/evolution movesets. */
    fun getLearnableTMs(player: ServerPlayer, pokemon: Pokemon): Collection<ResourceLocation> =
        Cobblemon.playerDataManager.getTMData(player).getLearnableTMsFromPokemon(pokemon)

    /**
     * Marks [tmId] as learned for [player] via Cobblemon's own [TMMoveManager.learn]
     * (handles client sync and advancement triggers). Returns false if [player] already
     * had it learned.
     */
    fun learn(player: ServerPlayer, tmId: ResourceLocation): Boolean =
        Cobblemon.playerDataManager.getTMData(player).learn(listOf(tmId))

    /**
     * Removes [tmId] from [player]'s learned set via Cobblemon's own [TMMoveManager.unlearn]
     * (handles client sync). Returns false if [player] didn't have it learned.
     */
    fun unlearn(player: ServerPlayer, tmId: ResourceLocation): Boolean =
        Cobblemon.playerDataManager.getTMData(player).unlearn(listOf(tmId))

    /** Whether [player] currently has [tmId] marked as learned. */
    fun hasLearned(player: ServerPlayer, tmId: ResourceLocation): Boolean =
        Cobblemon.playerDataManager.getTMData(player).learnedTMs.contains(tmId)
}
