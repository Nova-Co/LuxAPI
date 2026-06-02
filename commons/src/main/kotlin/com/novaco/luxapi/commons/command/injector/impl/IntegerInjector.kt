package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.sender.CommandSender

/**
 * An [ArgumentInjector] responsible for parsing and validating integer arguments from a command string.
 * It attempts to convert a string argument into an [Int].
 */
class IntegerInjector : ArgumentInjector<Int> {
    /**
     * The target class type that this injector handles, which is [Int].
     */
    override val convertedClass: Class<Int> = Int::class.javaObjectType

    /**
     * Instantiates an [Int] from the command arguments.
     *
     * @param sender The entity that executed the command.
     * @param args The array of string arguments from the command.
     * @param index The specific index in the args array to parse.
     * @return The parsed integer value.
     * @throws CommandParseException if the argument is missing or is not a valid integer.
     */
    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): Int {
        val arg = args.getOrNull(index)
            ?: throw CommandParseException("§cError: Missing integer argument at position ${index + 1}.")

        return arg.toIntOrNull()
            ?: throw CommandParseException("§cError: '$arg' is not a valid number!")
    }
}