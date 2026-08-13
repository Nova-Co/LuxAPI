package com.novaco.luxapi.cobblemon.types

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.api.types.tera.TeraType
import com.cobblemon.mod.common.api.types.tera.TeraTypes
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.chat.MutableComponent

/**
 * Query/registration wrapper around Cobblemon's [ElementalTypes] registry, plus Tera
 * type assignment on a live [Pokemon].
 *
 * **Scope note:** a Pokémon's primary/secondary [ElementalType] is derived from its
 * species form (`Pokemon.primaryType`/`secondaryType` are `get()`-only, backed by
 * `form.primaryType`/`form.secondaryType`) — vanilla Cobblemon has no per-instance
 * mutation point for those, so this wrapper doesn't expose one either. The only real
 * per-instance type mutation Cobblemon supports is [Pokemon.teraType].
 *
 * **Known gap:** [TeraTypes] has no public registration entry point (its `create()` is
 * `private`), so a custom [ElementalType] registered via [register] cannot be paired
 * with a matching [TeraType] — [getTeraType]/[setTeraType] only resolve Cobblemon's
 * own 18 built-in Tera types (plus Stellar).
 */
object ElementalTypeManager {

    fun all(): List<ElementalType> = ElementalTypes.all()

    fun get(name: String): ElementalType? = ElementalTypes.get(name)

    /**
     * Registers a new [ElementalType] via [ElementalTypes.register]. [hue] is the
     * type-chart color Cobblemon's own UI renders for this type; [textureXMultiplier]
     * is the column offset into its type-icon spritesheet — a custom type registered
     * this way has no icon of its own unless a matching resource pack ships one.
     */
    fun register(name: String, displayName: MutableComponent, hue: Int, textureXMultiplier: Int): ElementalType =
        ElementalTypes.register(name, displayName, hue, textureXMultiplier)

    fun getTeraType(id: String): TeraType? = TeraTypes.get(id)

    /**
     * Assigns [pokemon]'s Tera type by id (e.g. "fire", or a full "namespace:path").
     * Returns false without touching the Pokémon if [teraTypeId] doesn't resolve to a
     * known [TeraType]. Unlike [com.novaco.luxapi.cobblemon.pokemon.PokemonPropertyManager]'s
     * IV/EV setters, no readback is needed: Cobblemon's [Pokemon.teraType] setter has
     * no rejection logic to detect.
     */
    fun setTeraType(pokemon: Pokemon, teraTypeId: String): Boolean {
        val teraType = TeraTypes.get(teraTypeId) ?: return false
        pokemon.teraType = teraType
        return true
    }
}
