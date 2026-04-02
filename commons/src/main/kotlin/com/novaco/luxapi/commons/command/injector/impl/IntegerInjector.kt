package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.sender.CommandSender

class IntegerInjector : ArgumentInjector<Int> {
    override val convertedClass: Class<Int> = Int::class.javaObjectType

    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): Int {
        val arg = args.getOrNull(index)
            ?: throw CommandParseException("§cError: Missing integer argument at position ${index + 1}.")

        return arg.toIntOrNull()
            ?: throw CommandParseException("§cError: '$arg' is not a valid number!")
    }
}