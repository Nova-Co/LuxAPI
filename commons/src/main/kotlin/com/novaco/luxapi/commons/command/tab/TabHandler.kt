package com.novaco.luxapi.commons.command.tab

import com.novaco.luxapi.commons.command.sender.CommandSender

/**
 * Interface for providing dynamic tab completion suggestions.
 * Implement this interface to define custom auto-complete logic for specific data types.
 */
interface TabHandler {

    /**
     * Provides a list of suggestions based on the current command context.
     *
     * @param sender The entity (player or console) requesting the suggestions.
     * @param args The current raw arguments typed by the sender so far.
     * @return A list of strings to display in the client's tab-complete menu.
     */
    fun getSuggestions(sender: CommandSender, args: Array<String>): List<String>
}