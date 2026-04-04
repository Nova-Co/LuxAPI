package com.novaco.luxapi.commons.command.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommandParseExceptionTest {

    @Test
    fun `test exception retains error message`() {
        val errorMessage = "§cError: Missing integer argument."

        val exception = assertThrows(CommandParseException::class.java) {
            throw CommandParseException(errorMessage)
        }

        assertEquals(errorMessage, exception.message, "The exception must accurately retain the formatted error message.")
    }
}