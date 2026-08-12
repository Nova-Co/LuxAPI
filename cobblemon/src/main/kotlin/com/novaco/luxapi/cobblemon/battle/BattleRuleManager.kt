package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent
import net.minecraft.network.chat.Component

/**
 * Enforces global battle-entry constraints — species bans and level caps — by rejecting battles
 * before they start. Scoped to what Cobblemon 1.7.3 actually exposes: [BattleStartedEvent.Pre] is the
 * only cancellable pre-battle hook, and it only lets you inspect species/level of both sides' Pokemon.
 * Item restrictions and weather-forcing have no equivalent Cobblemon hook and are not supported.
 */
object BattleRuleManager {

    private val bannedSpecies = mutableSetOf<String>()
    private var minLevel: Int? = null
    private var maxLevel: Int? = null
    private var customRule: ((BattleStartedEvent.Pre) -> String?)? = null
    private var isRegistered = false

    /** Bans one or more species (matched case-insensitively by name, e.g. "mewtwo") from any battle. */
    fun banSpecies(vararg speciesId: String) {
        bannedSpecies.addAll(speciesId.map { it.lowercase() })
    }

    /** Removes a previously banned species. */
    fun unbanSpecies(vararg speciesId: String) {
        bannedSpecies.removeAll(speciesId.map { it.lowercase() }.toSet())
    }

    /** Sets a global level cap; either bound may be null to leave it unrestricted. */
    fun setLevelCap(min: Int? = null, max: Int? = null) {
        this.minLevel = min
        this.maxLevel = max
    }

    /**
     * Registers a custom rule evaluated after species/level checks pass. Return a non-null string to
     * reject the battle with that message as the reason; return null to allow it.
     */
    fun setCustomRule(rule: (BattleStartedEvent.Pre) -> String?) {
        this.customRule = rule
    }

    /** Subscribes to Cobblemon's pre-battle event. Safe to call multiple times — only subscribes once. */
    fun register() {
        if (isRegistered) return
        isRegistered = true

        CobblemonEvents.BATTLE_STARTED_PRE.subscribe { event ->
            val rejection = findViolation(event)
            if (rejection != null) {
                event.reason = Component.literal(rejection)
                event.cancel()
            }
        }
    }

    private fun findViolation(event: BattleStartedEvent.Pre): String? {
        val allPokemon = (event.getPokemonOnSide(1).orEmpty() + event.getPokemonOnSide(2).orEmpty())
            .mapNotNull { it.battlePokemon?.originalPokemon }

        for (pokemon in allPokemon) {
            val speciesName = pokemon.species.name.lowercase()
            if (speciesName in bannedSpecies) {
                return "${pokemon.species.name} is banned from battle."
            }
            minLevel?.let { min ->
                if (pokemon.level < min) return "${pokemon.species.name} is below the minimum battle level ($min)."
            }
            maxLevel?.let { max ->
                if (pokemon.level > max) return "${pokemon.species.name} is above the maximum battle level ($max)."
            }
        }

        customRule?.invoke(event)?.let { return it }

        return null
    }
}
