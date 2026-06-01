package com.novaco.luxapi.bukkit.command.tab

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.command.tab.TabHandler
import com.novaco.luxapi.commons.command.tab.TabUtils
import org.bukkit.Bukkit

/**
 * Platform-specific tab completion handler for Bukkit.
 * Responsible for dynamically suggesting the names of currently online players
 * when a command expects a player argument.
 */
class BukkitPlayerTabHandler : TabHandler {

    /**
     * Retrieves and filters a list of online player names based on the current command input.
     * Extracts all currently online players from the Bukkit server, and delegates the
     * filtering logic to [TabUtils] to match the user's partial input.
     *
     * @param sender The entity (player or console) requesting the tab completions.
     * @param args The current array of raw arguments typed by the sender so far.
     * @return A filtered list of online player names suitable for the client's auto-complete menu.
     */
    override fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> {
        val onlinePlayers = Bukkit.getOnlinePlayers().map { it.name }
        return TabUtils.filter(onlinePlayers, args)
    }
}