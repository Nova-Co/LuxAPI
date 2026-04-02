package com.novaco.luxapi.commons.command.tab

import com.novaco.luxapi.commons.command.sender.CommandSender

/**
 * Interface for providing dynamic tab completion suggestions.
 */
interface TabHandler {
    /**
     * Provides a list of suggestions based on the current command context.
     *
     * @param sender The entity requesting the suggestions.
     * @param args The current raw arguments typed so far.
     * @return A list of strings to suggest in the tab menu.
     */
    fun getSuggestions(sender: CommandSender, args: Array<String>): List<String>
}