package com.novaco.luxapi.cobblemon.chat

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.core.chat.SmartMessage
import net.minecraft.server.level.ServerPlayer

/**
 * An extensible formatter for generating Pokemon party summaries.
 * Developers can extend this class and override specific methods
 * to customize the layout, colors, and displayed data.
 *
 * This class uses the `SmartMessage` builder pattern to construct
 * a chat message with hoverable tooltips. By overriding the build*
 * functions, API consumers can deeply customize the appearance of
 * the party summary without having to rewrite the core logic.
 */
open class PartySummaryFormatter {

    /**
     * The primary execution method. Builds the entire message by iterating
     * through the provided PartyStore. This provides 100% freedom to format
     * parties from players, PCs, or NPCs.
     *
     * @param party The PartyStore containing the Pokemon.
     * @param ownerName The display name of the party's owner.
     * @return A constructed SmartMessage ready to be sent.
     */
    open fun format(party: PartyStore, ownerName: String): SmartMessage {
        val message = SmartMessage()

        message.append(buildHeader(ownerName))

        for (i in 0 until party.size()) {
            val pokemon = party.get(i)

            if (pokemon == null) {
                message.append(buildEmptySlot(i))
            } else {
                val prefix = buildSlotPrefix(i)
                val visibleText = buildVisibleText(pokemon)
                val hoverText = buildHoverText(pokemon)

                message.append(prefix)
                message.appendHoverText(visibleText, hoverText)
            }

            message.append(buildSeparator())
        }

        message.append(buildFooter(ownerName))

        return message
    }

    /**
     * Convenience method to format the active party of a specific player.
     *
     * @param player The ServerPlayer whose active party is being summarized.
     * @return A constructed SmartMessage ready to be sent.
     */
    open fun format(player: ServerPlayer): SmartMessage {
        val party = Cobblemon.storage.getParty(player)
        return format(party, player.name.string)
    }

    /**
     * Builds the header of the party summary.
     *
     * @param ownerName The name of the party's owner.
     * @return Formatted string for the header.
     */
    open fun buildHeader(ownerName: String): String {
        return "&8[&b&lParty Summary&8] &7of &a$ownerName\n"
    }

    /**
     * Builds the representation of an empty party slot.
     *
     * @param index The 0-based index of the empty slot.
     * @return Formatted string for the empty slot.
     */
    open fun buildEmptySlot(index: Int): String {
        return "&8[&7${index + 1}&8] &cEmpty Slot"
    }

    /**
     * Builds the prefix for an occupied party slot (usually the slot number).
     *
     * @param index The 0-based index of the occupied slot.
     * @return Formatted string for the slot prefix.
     */
    open fun buildSlotPrefix(index: Int): String {
        return "&8[&e${index + 1}&8] "
    }

    /**
     * Builds the main visible text for a Pokemon in the party.
     *
     * @param pokemon The Pokemon instance to display.
     * @return Formatted string for the Pokemon's visible entry.
     */
    open fun buildVisibleText(pokemon: Pokemon): String {
        val shinyTag = if (pokemon.shiny) "&e✨ " else ""
        val genderTag = when (pokemon.gender.name) {
            "MALE" -> "&b♂"
            "FEMALE" -> "&d♀"
            else -> "&7⚪"
        }
        val displayName = pokemon.species.name.replaceFirstChar { it.uppercase() }
        return "$shinyTag&a$displayName &7[Lvl &e${pokemon.level}&7] $genderTag"
    }

    /**
     * Builds the hover text (tooltip) for a Pokemon in the party.
     *
     * @param pokemon The Pokemon instance to inspect.
     * @return Formatted string for the hover tooltip.
     */
    open fun buildHoverText(pokemon: Pokemon): String {
        return buildString {
            append("&b&l${pokemon.species.name.replaceFirstChar { it.uppercase() }} Stats\n")
            append("&8-------------------\n")
            append("&7Ability: &e${pokemon.ability.name.replaceFirstChar { it.uppercase() }}\n")
            append("&7Nature: &e${pokemon.nature.displayName.replaceFirstChar { it.uppercase() }}\n")
            append("&7Friendship: &c${pokemon.friendship}\n")

            val heldItem = pokemon.heldItem()
            if (!heldItem.isEmpty) {
                append("&7Held Item: &6${heldItem.hoverName.string}\n")
            } else {
                append("&7Held Item: &8None\n")
            }

            append("\n&b&lMoves:\n")
            append(buildMovesText(pokemon))

            append("\n\n&b&lIVs &7/ &b&lEVs:\n")
            append(buildStatsText(pokemon))
        }
    }

    /**
     * Builds a formatted string representing the Pokemon's current move set.
     *
     * @param pokemon The Pokemon instance.
     * @return Formatted string listing the moves.
     */
    open fun buildMovesText(pokemon: Pokemon): String {
        val moves = pokemon.moveSet.getMoves()
        if (moves.isEmpty()) return "&7- None"

        return moves.joinToString("\n") { move ->
            "&7- &f${move.template.displayName.string}"
        }
    }

    /**
     * Builds a formatted string representing the Pokemon's IVs and EVs.
     *
     * @param pokemon The Pokemon instance.
     * @return Formatted string detailing the stats.
     */
    open fun buildStatsText(pokemon: Pokemon): String {
        val hpIv = pokemon.ivs.getOrDefault(Stats.HP)
        val atkIv = pokemon.ivs.getOrDefault(Stats.ATTACK)
        val defIv = pokemon.ivs.getOrDefault(Stats.DEFENCE)
        val spaIv = pokemon.ivs.getOrDefault(Stats.SPECIAL_ATTACK)
        val spdIv = pokemon.ivs.getOrDefault(Stats.SPECIAL_DEFENCE)
        val speIv = pokemon.ivs.getOrDefault(Stats.SPEED)

        val hpEv = pokemon.evs.getOrDefault(Stats.HP)
        val atkEv = pokemon.evs.getOrDefault(Stats.ATTACK)
        val defEv = pokemon.evs.getOrDefault(Stats.DEFENCE)
        val spaEv = pokemon.evs.getOrDefault(Stats.SPECIAL_ATTACK)
        val spdEv = pokemon.evs.getOrDefault(Stats.SPECIAL_DEFENCE)
        val speEv = pokemon.evs.getOrDefault(Stats.SPEED)

        return buildString {
            append("&7HP: &a$hpIv &8(&e$hpEv&8)  ")
            append("&7Atk: &a$atkIv &8(&e$atkEv&8)  ")
            append("&7Def: &a$defIv &8(&e$defEv&8)\n")
            append("&7SpA: &a$spaIv &8(&e$spaEv&8)  ")
            append("&7SpD: &a$spdIv &8(&e$spdEv&8)  ")
            append("&7Spe: &a$speIv &8(&e$speEv&8)")
        }
    }

    /**
     * Builds the separator appended after each party slot.
     * Defaults to a newline. Override to create horizontal lists.
     *
     * @return Formatted string for the separator.
     */
    open fun buildSeparator(): String {
        return "\n"
    }

    /**
     * Builds the footer displayed at the very bottom of the summary.
     * Defaults to empty. Override to add concluding text or statistics.
     *
     * @param ownerName The name of the party's owner.
     * @return Formatted string for the footer.
     */
    open fun buildFooter(ownerName: String): String {
        return ""
    }
}