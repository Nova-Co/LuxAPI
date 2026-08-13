package com.novaco.luxapi.cobblemon.ability

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Query/registration wrapper around Cobblemon's [Abilities] registry, plus safe
 * assignment of an ability onto a live [Pokemon] via [Pokemon.updateAbility].
 *
 * **Scope note:** Cobblemon's own ability battle-mechanics are driven by a Showdown JS
 * script loaded per ability id ([Abilities] loads these from datapack `abilities/`
 * `.js` resources at startup, via internals not exposed publicly). A template registered
 * here has a name/display name but no mechanical effect in battle unless a matching
 * script is also shipped in a datapack — this wrapper covers naming/assignment, not
 * battle-scripting.
 */
object AbilityManager {

    fun all(): List<AbilityTemplate> = Abilities.all()

    fun get(name: String): AbilityTemplate? = Abilities.get(name)

    /**
     * Registers a new [AbilityTemplate] under [name] via [Abilities.register]. Note
     * Cobblemon's own registry silently overwrites whatever was previously registered
     * under the same (lowercased) name — check [get] first if that matters.
     */
    fun register(name: String): AbilityTemplate = Abilities.register(AbilityTemplate(name))

    /**
     * Looks up [name] in the [Abilities] registry and assigns it to [pokemon] via
     * [Pokemon.updateAbility]. Returns false without touching the Pokémon if [name]
     * doesn't resolve to a known ability.
     *
     * @param forced When true, the ability is pinned regardless of future species/form
     * changes. When false, Cobblemon's own [Pokemon.updateAbility] still pins it anyway
     * if [name] isn't one of the species' natural ability options — that's Cobblemon
     * behavior, not a bug in this wrapper.
     */
    fun setAbility(pokemon: Pokemon, name: String, forced: Boolean = false): Boolean {
        val template = Abilities.get(name) ?: return false
        val assigned = pokemon.updateAbility(template.create(forced))
        return assigned.template == template
    }
}
