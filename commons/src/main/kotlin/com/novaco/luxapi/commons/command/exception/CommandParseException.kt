package com.novaco.luxapi.commons.command.exception

/**
 * Exception thrown when a command argument fails to parse into the requested data type.
 *
 * @param errorMessage The user-friendly error message that will be dispatched to the command sender.
 */
class CommandParseException(val errorMessage: String) : RuntimeException(errorMessage)