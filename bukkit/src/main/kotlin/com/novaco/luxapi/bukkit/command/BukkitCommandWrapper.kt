package com.novaco.luxapi.bukkit.command

import com.novaco.luxapi.commons.command.CommandProcessor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * Acts as a bridge between Bukkit's native command execution and LuxAPI's processing logic.
 */
class BukkitCommandWrapper(
    name: String,
    description: String,
    usageMessage: String,
    aliases: List<String>,
    private val processor: CommandProcessor
) : Command(name, description, usageMessage, aliases) {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val luxSender = BukkitCommandSender(sender)
        val safeArgs = args.filterNotNull().toTypedArray()

        processor.process(luxSender, safeArgs)
        return true
    }

    @Throws(IllegalArgumentException::class)
    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        val luxSender = BukkitCommandSender(sender)
        val safeArgs = args.filterNotNull().toTypedArray()
        return processor.getSuggestions(luxSender, safeArgs, emptyList())
    }
}