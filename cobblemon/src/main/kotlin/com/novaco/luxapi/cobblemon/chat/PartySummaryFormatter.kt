package com.novaco.luxapi.cobblemon.chat

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.core.chat.SmartMessage
import net.minecraft.server.level.ServerPlayer

/**
 * A utility class for generating highly interactive and visually appealing
 * summaries of a player's Pokemon party.
 * Utilizes the core SmartMessage API for hoverable tooltips.
 */
object PartySummaryFormatter {

    /**
     * Generates a beautifully formatted SmartMessage summarizing the player's current party.
     *
     * @param player The target player whose party will be summarized.
     * @return A SmartMessage ready to be sent or broadcasted.
     */
    fun format(player: ServerPlayer): SmartMessage {
        val party = Cobblemon.storage.getParty(player)
        val message = SmartMessage()

        message.append("&8[&b&lParty Summary&8] &7of &a${player.name.string}\n")

        for (i in 0 until party.size()) {
            val pokemon = party.get(i)

            if (pokemon == null) {
                message.append("&8[&7${i + 1}&8] &cEmpty Slot\n")
                continue
            }

            message.append("&8[&e${i + 1}&8] ")
            formatPokemonLine(message, pokemon)
            message.append("\n")
        }

        return message
    }

    /**
     * Helper method to format a single Pokemon's line with interactive hover stats.
     */
    private fun formatPokemonLine(message: SmartMessage, pokemon: Pokemon) {
        val shinyTag = if (pokemon.shiny) "&e✨ " else ""
        val genderTag = when (pokemon.gender.name) {
            "MALE" -> "&b♂"
            "FEMALE" -> "&d♀"
            else -> "&7⚪"
        }

        val displayName = pokemon.species.name.capitalize()
        val level = pokemon.level

        val visibleText = "$shinyTag&a$displayName &7[Lvl &e$level&7] $genderTag"

        val hoverText = buildString {
            append("&b&l${displayName} Stats\n")
            append("&8-------------------\n")
            append("&7Ability: &e${pokemon.ability.name}\n")
            append("&7Nature: &e${pokemon.nature.name}\n")
            append("&7Friendship: &c${pokemon.friendship}\n")

            val heldItem = pokemon.heldItem()
            if (!heldItem.isEmpty) {
                append("&7Held Item: &6${heldItem.hoverName.string}\n")
            } else {
                append("&7Held Item: &8None\n")
            }

            // สามารถเพิ่ม IVs/EVs โชว์ตรงนี้ในอนาคตได้สบายๆ ครับ
        }

        message.appendHoverText(visibleText, hoverText)
    }
}