package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.sender.CommandSender

/**
 * An [ArgumentInjector] responsible for parsing and validating a simple string argument from a command.
 * It essentially just retrieves the string at the specified index.
 */
class StringInjector : ArgumentInjector<String> {
    /**
     * The target class type that this injector handles, which is [String].
     */
    override val convertedClass: Class<String> = String::class.java

    /**
     * Instantiates a [String] from the command arguments.
     *
     * @param sender The entity that executed the command.
     * @param args The array of string arguments from the command.
     * @param index The specific index in the args array to retrieve.
     * @return The string argument at the given index.
     * @throws CommandParseException if the argument is missing at the specified index.
     */
    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): String {
        return args.getOrNull(index)
            ?: throw CommandParseException("§cError: Missing string argument at position ${index + 1}.")
    }
}