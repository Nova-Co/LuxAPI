package com.novaco.luxapi.commons.command.scanning

import com.novaco.luxapi.commons.command.CommandManager
import com.novaco.luxapi.commons.command.annotation.Command
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Command(name = "scanned")
class ScannableTestCommand

abstract class UnscannableAbstractCommand

private class RecordingCommandManager : CommandManager {
    val registered = mutableListOf<Any>()
    override fun register(commandInstance: Any) {
        registered.add(commandInstance)
    }
}

class CommandScannerTest {

    @Test
    fun `test scanAndRegister discovers and registers annotated classes on the test classpath`() {
        val manager = RecordingCommandManager()

        val count = CommandScanner.scanAndRegister(manager, "com.novaco.luxapi.commons.command.scanning")

        assertTrue(count >= 1, "Expected at least the ScannableTestCommand to be discovered")
        assertTrue(manager.registered.any { it is ScannableTestCommand })
        assertTrue(manager.registered.none { it is UnscannableAbstractCommand })
    }

    @Test
    fun `test registerAll forwards every instance to the manager`() {
        val manager = RecordingCommandManager()
        val instances = listOf(ScannableTestCommand(), ScannableTestCommand())

        CommandScanner.registerAll(manager, instances)

        assertEquals(2, manager.registered.size)
    }
}
