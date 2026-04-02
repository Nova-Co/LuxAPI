package com.novaco.luxapi.commons.command.exception

/**
 * Thrown when an argument fails to parse into the requested type.
 * * @param errorMessage The message that will be automatically sent to the CommandSender.
 */
class CommandParseException(val errorMessage: String) : RuntimeException(errorMessage)