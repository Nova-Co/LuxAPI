package com.novaco.luxapi.bukkit.command

import com.novaco.luxapi.commons.command.CommandProcessor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * Acts as a bridge between Bukkit's native command execution system and LuxAPI's core processing logic.
 * This wrapper intercepts standard Bukkit commands and forwards them to the internal [CommandProcessor]
 * after adapting the command sender and safely filtering the arguments.
 */
class BukkitCommandWrapper(
    name: String,
    description: String,
    usageMessage: String,
    aliases: List<String>,
    private val processor: CommandProcessor
) : Command(name, description, usageMessage, aliases) {

    /**
     * Executes the given command.
     * Translates the Bukkit [CommandSender] into a LuxAPI-compatible sender and forwards
     * the execution request to the underlying [CommandProcessor].
     *
     * @param sender The source of the command execution (e.g., player, console, or command block).
     * @param commandLabel The exact command alias that was used.
     * @param args The array of arguments passed to the command.
     * @return true if the command was successfully processed.
     */
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val luxSender = BukkitCommandSender(sender)
        val safeArgs = args.filterNotNull().toTypedArray()

        processor.process(luxSender, safeArgs)
        return true
    }

    /**
     * Requests a list of possible completions for a command argument.
     * Delegates the auto-completion logic to the [CommandProcessor] to fetch
     * dynamic, context-aware suggestions based on the user's current input.
     *
     * @param sender The source requesting the tab completions.
     * @param alias The exact command alias that was used.
     * @param args The current command arguments typed by the sender so far.
     * @return A list of suggested string completions.
     * @throws IllegalArgumentException If an error occurs during the generation of suggestions.
     */
    @Throws(IllegalArgumentException::class)
    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        val luxSender = BukkitCommandSender(sender)
        val safeArgs = args.filterNotNull().toTypedArray()

        return processor.getSuggestions(luxSender, safeArgs)
    }
}