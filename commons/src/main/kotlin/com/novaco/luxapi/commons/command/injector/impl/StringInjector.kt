package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.sender.CommandSender

class StringInjector : ArgumentInjector<String> {
    override val convertedClass: Class<String> = String::class.java

    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): String {
        return args.getOrNull(index)
            ?: throw CommandParseException("§cError: Missing string argument at position ${index + 1}.")
    }
}