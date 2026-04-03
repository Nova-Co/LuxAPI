package com.novaco.luxapi.commons.text

import com.novaco.luxapi.commons.player.LuxPlayer
import kotlin.math.ceil

/**
 * A utility class to automatically split long lists of text into chat pages.
 * Utilizes MiniMessage to generate interactive, clickable navigation buttons.
 */
class ChatPaginator(
    private val items: List<String>,
    private val linesPerPage: Int = 8,
    private val header: String = "<gold>--- <bold>Menu</bold> ---</gold>",
    private val commandPrefix: String
) {

    /**
     * Calculates the total number of pages required to display all items.
     */
    val totalPages: Int
        get() = maxOf(1, ceil(items.size.toDouble() / linesPerPage).toInt())

    /**
     * Sends a specific page of text to the player, complete with header and interactive footer.
     *
     * @param player The target player to receive the messages.
     * @param pageNumber The page number to display (1-based index).
     */
    fun sendPage(player: LuxPlayer, pageNumber: Int) {
        val validPage = pageNumber.coerceIn(1, totalPages)

        val startIndex = (validPage - 1) * linesPerPage
        val endIndex = minOf(startIndex + linesPerPage, items.size)

        player.sendMessage(header)

        if (items.isEmpty()) {
            player.sendMessage("<gray><i>No entries found.</i></gray>")
        } else {
            for (i in startIndex until endIndex) {
                player.sendMessage(items[i])
            }
        }

        player.sendMessage(buildFooter(validPage))
    }

    /**
     * Constructs the footer string containing clickable MiniMessage buttons.
     */
    private fun buildFooter(currentPage: Int): String {
        val footer = StringBuilder("<gray>")

        // [<<<] Previous Page Button
        if (currentPage > 1) {
            val prevCmd = "$commandPrefix ${currentPage - 1}"
            footer.append("<click:run_command:'$prevCmd'><hover:show_text:'<gray>Click for Previous Page'><green>[<<<]</green></hover></click> ")
        } else {
            footer.append("<dark_gray>[<<<]</dark_gray> ") // Disabled state
        }

        footer.append("<yellow>Page $currentPage of $totalPages</yellow>")

        // [>>>] Next Page Button
        if (currentPage < totalPages) {
            val nextCmd = "$commandPrefix ${currentPage + 1}"
            footer.append(" <click:run_command:'$nextCmd'><hover:show_text:'<gray>Click for Next Page'><green>[>>>]</green></hover></click>")
        } else {
            footer.append(" <dark_gray>[>>>]</dark_gray>") // Disabled state
        }

        footer.append("</gray>")
        return footer.toString()
    }
}