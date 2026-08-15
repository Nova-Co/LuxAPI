package com.novaco.luxapi.bukkit.command.injector

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.CompletingInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import org.bukkit.Bukkit
import org.bukkit.World

/**
 * Platform-specific argument injector for Bukkit's native [World] class.
 * Resolves a world name argument into the matching loaded [World].
 */
class BukkitWorldInjector : CompletingInjector<World> {

    override val convertedClass: Class<World> = World::class.java

    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): World {
        val worldName = args.getOrNull(index)
            ?: throw CommandParseException("§cError: Please specify a world name.")

        return Bukkit.getWorld(worldName)
            ?: throw CommandParseException("§cError: Could not find world '$worldName'.")
    }

    override fun getSuggestions(sender: CommandSender, args: Array<String>, index: Int): List<String> {
        val partial = args.getOrNull(index)?.lowercase() ?: ""
        return Bukkit.getWorlds()
            .map { it.name }
            .filter { it.lowercase().startsWith(partial) }
    }
}
