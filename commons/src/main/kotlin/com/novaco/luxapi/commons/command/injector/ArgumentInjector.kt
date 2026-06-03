package com.novaco.luxapi.commons.command.injector

import com.novaco.luxapi.commons.command.sender.CommandSender

/**
 * Defines a contract for converting a raw string argument into a specific object type.
 */
interface ArgumentInjector<T> {

    /**
     * The target class type this injector is responsible for converting.
     */
    val convertedClass: Class<T>

    /**
     * Transforms the string argument at the specified index into the target object type.
     *
     * @param sender The entity executing the command.
     * @param args The full array of string arguments provided in the command.
     * @param index The current index of the argument being evaluated.
     * @return The instantiated object, or null if the conversion fails.
     */
    fun instantiate(sender: CommandSender, args: Array<String>, index: Int): T?
}