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

    /**
     * Executes the command, translating the Bukkit sender to a LuxAPI sender.
     */
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val luxSender = BukkitCommandSender(sender)
        val safeArgs = args.filterNotNull().toTypedArray()

        processor.process(luxSender, safeArgs)
        return true
    }

    /**
     * Requests a list of possible completions for a command argument.
     * Fetches online players dynamically to supply to the suggestion engine.
     */
    @Throws(IllegalArgumentException::class)
    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        val luxSender = BukkitCommandSender(sender)
        val safeArgs = args.filterNotNull().toTypedArray()
        val onlinePlayers = Bukkit.getOnlinePlayers().map { it.name }

        val suggestions = processor.getSuggestions(luxSender, safeArgs, onlinePlayers)

        if (suggestions.isEmpty() && safeArgs.isNotEmpty()) {
            val currentInput = safeArgs.last().lowercase()
            val isSubCommandMatch = safeArgs.size >= 2 && (safeArgs[0].equals("forcesync", ignoreCase = true) || safeArgs[0].equals("audit", ignoreCase = true))

            if (isSubCommandMatch) {
                val results = mutableListOf<String>()

                if (safeArgs[0].equals("forcesync", ignoreCase = true) && "all".startsWith(currentInput)) {
                    results.add("all")
                }

                results.addAll(onlinePlayers.filter { it.lowercase().startsWith(currentInput) })
                return results
            }
        }

        return suggestions
    }
}