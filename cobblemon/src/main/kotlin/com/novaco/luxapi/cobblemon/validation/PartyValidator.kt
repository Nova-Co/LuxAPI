package com.novaco.luxapi.cobblemon.validation

import com.cobblemon.mod.common.util.party
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

object PartyValidator {

    /**
     * Common rule presets for easy access.
     */
    val SMOGON_OU_RULES = listOf(SpeciesClause(), ItemClause())
    val DRAFT_LEAGUE_RULES = listOf(SpeciesClause(), NoLegendariesRule())

    /**
     * Validates a player's active party against a list of rules.
     *
     * @param player The target player to validate.
     * @param rules A list of PartyRules to check against.
     * @return ValidationResult indicating success or the first failure reason.
     */
    fun validate(player: LuxPlayer, rules: List<PartyRule>): ValidationResult {
        val serverPlayer = player.parent as ServerPlayer
        val party = serverPlayer.party()

        val activeRoster = party.filterNotNull().filter { it.currentHealth > 0 }

        if (activeRoster.isEmpty()) {
            return ValidationResult(false, "Your party has no battle-ready Pokémon.")
        }

        for (rule in rules) {
            val result = rule.validate(activeRoster)
            if (!result.isValid) {
                return result
            }
        }

        return ValidationResult(true)
    }
}