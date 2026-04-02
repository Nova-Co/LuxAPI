package com.novaco.luxapi.commons.command.injector

import com.novaco.luxapi.commons.command.sender.CommandSender

/**
 * Responsible for converting a raw string argument into a specific object type [T].
 */
interface ArgumentInjector<T> {

    /**
     * The target class type this injector is responsible for converting to.
     */
    val convertedClass: Class<T>

    /**
     * Converts the string argument at the given index into the target type.
     *
     * @param sender The entity executing the command.
     * @param args The full array of string arguments provided in the command.
     * @param index The current index of the argument being parsed.
     * @return The converted object, or null if parsing fails.
     */
    fun instantiate(sender: CommandSender, args: Array<String>, index: Int): T?
}