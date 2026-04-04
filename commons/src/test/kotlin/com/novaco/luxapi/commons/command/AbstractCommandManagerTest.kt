package com.novaco.luxapi.commons.command

import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.annotation.SubCommand
import com.novaco.luxapi.commons.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Dummy command classes for testing registration logic.
 * FIXED: Added a default 'execute' method without a @SubCommand annotation.
 * The CommandProcessor enforces that a main fallback method must exist!
 */
@Command(name = "first", aliases = ["f1"], permission = "luxapi.first")
class CommandOne {

    // The main execution method (No @SubCommand annotation)
    fun execute(sender: CommandSender) {
        // Base command logic
    }

    @SubCommand(name = "run", permission = "luxapi.first.run")
    fun run(sender: CommandSender) {
        // Sub-command execution
    }
}

@Command(name = "second", aliases = ["f2"], permission = "luxapi.second")
class CommandTwo {

    // The main execution method (No @SubCommand annotation)
    fun execute(sender: CommandSender) {
        // Base command logic
    }

    @SubCommand(name = "run", permission = "luxapi.second.run")
    fun run(sender: CommandSender) {
        // Sub-command execution
    }
}

class AbstractCommandManagerTest {

    /**
     * A concrete implementation of AbstractCommandManager for testing.
     */
    class TestCommandManager : AbstractCommandManager() {
        var platformRegisterCount = 0

        override fun registerToPlatform(processor: CommandProcessor) {
            // Track how many times the platform was signaled
            platformRegisterCount++
        }
    }

    private lateinit var manager: TestCommandManager

    @BeforeEach
    fun setup() {
        manager = TestCommandManager()
    }

    @Test
    fun `test multiple command registration logic`() {
        // Register our valid dummy commands
        manager.register(CommandOne())
        manager.register(CommandTwo())

        val registeredNames = manager.getRegisteredCommandNames()
        assertEquals(2, registeredNames.size, "The manager should contain exactly 2 registered commands.")
        assertTrue(registeredNames.contains("first"), "Manager should contain 'first' command.")
        assertTrue(registeredNames.contains("second"), "Manager should contain 'second' command.")

        val processor = manager.getCommand("first")
        assertNotNull(processor, "Should be able to retrieve the CommandProcessor for 'first'.")
        assertEquals("first", processor?.commandInfo?.name, "The processor should hold the correct command metadata.")

        assertEquals(2, manager.platformRegisterCount, "The platform registration method should have been triggered twice.")
    }

    @Test
    fun `test command case insensitivity`() {
        manager.register(CommandOne()) // Name is "first"

        // Retrieval should work regardless of case
        assertNotNull(manager.getCommand("FIRST"), "Command retrieval should be case-insensitive.")
        assertNotNull(manager.getCommand("FiRsT"), "Command retrieval should be case-insensitive.")
    }
}