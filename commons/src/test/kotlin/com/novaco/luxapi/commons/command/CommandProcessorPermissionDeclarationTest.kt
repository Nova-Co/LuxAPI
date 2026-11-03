package com.novaco.luxapi.commons.command

import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.annotation.SubCommand
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.permission.PermissionChecker
import com.novaco.luxapi.commons.service.ServiceManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Command(name = "declaredperms", permission = "luxapi.test.main")
class DeclaredPermissionsCommand {
    fun execute(sender: CommandSender) {}

    @SubCommand(name = "reload", permission = "luxapi.test.reload")
    fun reload(sender: CommandSender) {}

    @SubCommand(name = "open")
    fun open(sender: CommandSender) {}
}

@Command(name = "openperms")
class UndeclaredPermissionsCommand {
    fun execute(sender: CommandSender) {}
}

class CommandProcessorPermissionDeclarationTest {

    private class RecordingChecker : PermissionChecker {
        val declared = mutableListOf<String>()
        override fun has(sender: CommandSender, node: String) = true
        override fun declare(node: String) {
            declared += node
        }
    }

    private val checker = RecordingChecker()

    @BeforeEach
    fun register() {
        ServiceManager.register(PermissionChecker::class.java, checker)
    }

    @AfterEach
    fun unregister() {
        ServiceManager.unregister(PermissionChecker::class.java)
    }

    @Test
    fun `declares the command and every non-blank sub-command permission`() {
        CommandProcessor(DeclaredPermissionsCommand())

        assertEquals(setOf("luxapi.test.main", "luxapi.test.reload"), checker.declared.toSet())
    }

    @Test
    fun `declares nothing for a command with no permissions`() {
        CommandProcessor(UndeclaredPermissionsCommand())

        assertTrue(checker.declared.isEmpty())
    }
}
