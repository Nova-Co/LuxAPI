package com.novaco.luxapi.cobblemon.mark

import com.cobblemon.mod.common.api.mark.Mark
import com.cobblemon.mod.common.api.mark.Marks
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation

/**
 * Query wrapper around Cobblemon's [Marks] registry, plus give/remove/active-mark
 * mutation on a live [Pokemon] via Cobblemon's own [Pokemon.exchangeMark] /
 * [Pokemon.activeMark].
 *
 * **Scope note:** [Marks] is a JSON-loaded registry with no public registration entry
 * point (data loads from datapack `marks/` resources only) — this wrapper is query +
 * Pokémon-side give/remove/activate only, the same shape
 * [com.novaco.luxapi.cobblemon.move.MoveManager] uses for its own JSON-loaded registry.
 */
object MarkManager {

    fun all(): List<Mark> = Marks.all()

    fun get(identifier: ResourceLocation): Mark? = Marks.getByIdentifier(identifier)

    /**
     * Gives [pokemon] [mark] via Cobblemon's own [Pokemon.exchangeMark]. A mark
     * declaring a `replace` group (mutually exclusive marks) has those removed first —
     * Cobblemon's own behavior, not this wrapper's. Returns true once [pokemon] is
     * confirmed to hold the mark (a no-op, not a failure, if it already did).
     */
    fun giveMark(pokemon: Pokemon, mark: Mark): Boolean {
        pokemon.exchangeMark(mark, give = true)
        return pokemon.marks.contains(mark)
    }

    /**
     * Removes [mark] from [pokemon] via Cobblemon's own [Pokemon.exchangeMark], which
     * also clears [Pokemon.activeMark] if that was the mark removed. Returns true once
     * confirmed removed (a no-op, not a failure, if the Pokémon never had it).
     */
    fun removeMark(pokemon: Pokemon, mark: Mark): Boolean {
        pokemon.exchangeMark(mark, give = false)
        return !pokemon.marks.contains(mark)
    }

    /**
     * Sets [pokemon]'s displayed [Pokemon.activeMark]. Cobblemon doesn't require the
     * mark to be one [pokemon] actually owns (see [Pokemon.marks]) for this to take
     * effect — that validation, if wanted, is the caller's responsibility.
     */
    fun setActiveMark(pokemon: Pokemon, mark: Mark?) {
        pokemon.activeMark = mark
    }
}
