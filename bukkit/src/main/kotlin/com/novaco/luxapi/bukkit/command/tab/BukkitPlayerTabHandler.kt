package com.novaco.luxapi.bukkit.command.tab

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.command.tab.TabHandler
import com.novaco.luxapi.commons.command.tab.TabUtils
import org.bukkit.Bukkit

/**
 * Platform-specific tab handler for providing online Bukkit players.
 */
class BukkitPlayerTabHandler : TabHandler {

    override fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> {
        val onlinePlayers = Bukkit.getOnlinePlayers().map { it.name }
        return TabUtils.filter(onlinePlayers, args)
    }
}