package com.novaco.luxapi.cobblemon.move

import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Query wrapper around Cobblemon's [Moves] registry, plus safe learn/unlearn/swap
 * mutation on a live [Pokemon]'s moveset.
 *
 * **Scope note:** Cobblemon's move data (power, PP, Showdown battle-effect script) is
 * loaded entirely from bundled Showdown JS resources at startup and [Moves]' own
 * `register` is `private` — unlike [com.novaco.luxapi.cobblemon.ability.AbilityManager]
 * / [com.novaco.luxapi.cobblemon.types.ElementalTypeManager], there is no runtime
 * custom-move registration surface here; this wrapper is query + Pokémon-side
 * learn/unlearn/swap only.
 */
object MoveManager {

    fun all(): List<MoveTemplate> = Moves.all()

    fun get(name: String): MoveTemplate? = Moves.getByName(name)

    fun getById(numericalId: Int): MoveTemplate? = Moves.getByNumericalId(numericalId)

    /**
     * Teaches [pokemon] [template] via Cobblemon's own [Pokemon.moveSet].add, which
     * places it in the first open slot with full PP. Returns false if the Pokémon
     * already knows the move or its moveset is already full (all 4 slots occupied).
     */
    fun learnMove(pokemon: Pokemon, template: MoveTemplate): Boolean = pokemon.moveSet.add(template.create())

    /**
     * Looks up [name] in the [Moves] registry and teaches it to [pokemon] (see the
     * [MoveTemplate] overload). Returns false without touching the Pokémon if [name]
     * doesn't resolve to a known move.
     */
    fun learnMove(pokemon: Pokemon, name: String): Boolean {
        val template = Moves.getByName(name) ?: return false
        return learnMove(pokemon, template)
    }

    /**
     * Removes [template] from [pokemon]'s moveset and benched moves via Cobblemon's
     * own [Pokemon.unlearnMove]. Always returns true (a no-op, not a failure, if the
     * Pokémon didn't know the move).
     */
    fun unlearnMove(pokemon: Pokemon, template: MoveTemplate): Boolean {
        pokemon.unlearnMove(template)
        return true
    }

    /**
     * Looks up [name] in the [Moves] registry and unlearns it from [pokemon] (see the
     * [MoveTemplate] overload). Returns false without touching the Pokémon if [name]
     * doesn't resolve to a known move.
     */
    fun unlearnMove(pokemon: Pokemon, name: String): Boolean {
        val template = Moves.getByName(name) ?: return false
        return unlearnMove(pokemon, template)
    }

    /**
     * Replaces [pokemon]'s move at moveset slot [position] (0-3) with [template] at
     * full PP, via Cobblemon's own [MoveSet.setMove]. Returns false if [position] is
     * out of range.
     */
    fun setMove(pokemon: Pokemon, position: Int, template: MoveTemplate): Boolean {
        if (position !in 0 until MoveSet.MOVE_COUNT) return false
        pokemon.moveSet.setMove(position, template.create())
        return true
    }

    /**
     * Looks up [name] in the [Moves] registry and sets it at moveset slot [position]
     * (see the [MoveTemplate] overload). Returns false without touching the Pokémon
     * if [name] doesn't resolve to a known move.
     */
    fun setMove(pokemon: Pokemon, position: Int, name: String): Boolean {
        val template = Moves.getByName(name) ?: return false
        return setMove(pokemon, position, template)
    }
}
