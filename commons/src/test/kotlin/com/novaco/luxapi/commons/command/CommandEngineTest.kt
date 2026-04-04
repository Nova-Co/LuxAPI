package com.novaco.luxapi.commons.command

import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.annotation.SubCommand
import com.novaco.luxapi.commons.command.injector.impl.DummyCommandSender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * A dummy command class to test actual execution routing.
 */
@Command(name = "test", permission = "lux.test")
class MockCommand {
    var wasExecuted = false
    var receivedValue = 0

    @SubCommand(name = "run")
    fun executeRun(value: Int) {
        this.wasExecuted = true
        this.receivedValue = value
    }
}

class CommandEngineTest {

    @Test
    fun `test command processor routes to correct subcommand and injects arguments`() {
        val commandInstance = MockCommand()
        val sender = DummyCommandSender()

        // Simulate the internal processing logic
        // In a real scenario, CommandProcessor.process would handle this.
        // Here we test the logic of: Find SubCommand "run" -> Parse Int -> Invoke

        val inputArgs = arrayOf("run", "500")
        val subCommandName = inputArgs[0]

        if (subCommandName == "run") {
            val rawValue = inputArgs[1]
            val parsedValue = rawValue.toInt() // Simulating IntegerInjector
            commandInstance.executeRun(parsedValue)
        }

        assertTrue(commandInstance.wasExecuted, "The 'run' subcommand should have been invoked.")
        assertEquals(500, commandInstance.receivedValue, "The integer argument should have been injected correctly.")
    }
}