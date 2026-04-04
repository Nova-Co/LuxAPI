package com.novaco.luxapi.commons.command.tab

/**
 * Utility class designed to assist with common tab-completion operations.
 * Ensures that suggestions are properly filtered before being sent to the client.
 */
object TabUtils {

    /**
     * Filters a list of raw suggestions based on the last argument typed by the sender.
     * Automatically handles case-insensitive partial matching.
     *
     * @param suggestions The complete list of possible completions.
     * @param args The current command arguments array.
     * @return A filtered list containing only suggestions that start with the user's current input.
     */
    fun filter(suggestions: List<String>, args: Array<String>): List<String> {
        if (args.isEmpty() || suggestions.isEmpty()) {
            return suggestions
        }

        val lastInput = args.last().lowercase()

        return suggestions.filter { it.lowercase().startsWith(lastInput) }
    }
}