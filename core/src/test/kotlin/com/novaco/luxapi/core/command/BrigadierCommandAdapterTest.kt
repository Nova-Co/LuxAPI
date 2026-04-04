package com.novaco.luxapi.core.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class BrigadierCommandAdapterTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test adapter registers base command with zero arguments`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mock(CommandSourceStack::class.java)
        `when`(mockSource.hasPermission(2)).thenReturn(true)

        var wasExecuted = false
        var capturedArgs: Array<String>? = null

        // 1. Register the command using our adapter
        BrigadierCommandAdapter.register(dispatcher, "heal", null) { _, args ->
            wasExecuted = true
            capturedArgs = args
        }

        // 2. Execute a simulated chat string
        dispatcher.execute("heal", mockSource)

        // 3. Verify
        assertTrue(wasExecuted, "The core handler should be triggered.")
        assertEquals(0, capturedArgs?.size, "Arguments array should be completely empty.")
    }

    @Test
    fun `test adapter captures and splits greedy arguments`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mock(CommandSourceStack::class.java)
        `when`(mockSource.hasPermission(2)).thenReturn(true)

        var capturedArgs: Array<String> = emptyArray()

        BrigadierCommandAdapter.register(dispatcher, "msg", null) { _, args ->
            capturedArgs = args
        }

        // Simulate typing a command with multiple arguments and irregular spaces
        dispatcher.execute("msg NovacoAdmin   Hello there!", mockSource)

        assertEquals(3, capturedArgs.size, "Should split the greedy string into exactly 3 arguments.")
        assertEquals("NovacoAdmin", capturedArgs[0])
        assertEquals("Hello", capturedArgs[1])
        assertEquals("there!", capturedArgs[2])
    }

    @Test
    fun `test command requires permission node`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mock(CommandSourceStack::class.java)

        var wasExecuted = false

        // Register with a dummy permission
        BrigadierCommandAdapter.register(dispatcher, "admin", "luxapi.admin") { _, _ ->
            wasExecuted = true
        }

        // Scenario A: Player does NOT have permission
        `when`(mockSource.hasPermission(2)).thenReturn(false)
        assertThrows(CommandSyntaxException::class.java) {
            // Brigadier naturally throws an exception if the 'requires' block fails
            dispatcher.execute("admin", mockSource)
        }
        assertFalse(wasExecuted, "Handler should not execute without permission.")

        // Scenario B: Player HAS permission
        `when`(mockSource.hasPermission(2)).thenReturn(true)
        assertDoesNotThrow {
            dispatcher.execute("admin", mockSource)
        }
        assertTrue(wasExecuted, "Handler should execute when permission is granted.")
    }
}