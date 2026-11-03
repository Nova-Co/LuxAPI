package com.novaco.luxapi.commons.permission

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.service.ServiceManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class LuxPermissionsTest {

    private val sender = object : CommandSender {
        override val name = "tester"
        override val uniqueId: UUID? = UUID.randomUUID()
        override fun sendMessage(message: String) {}
        override fun hasPermission(permission: String): Boolean = error("must not be called by LuxPermissions")
    }

    private class RecordingChecker(private val answer: (String) -> Boolean) : PermissionChecker {
        val declared = mutableListOf<String>()
        override fun has(sender: CommandSender, node: String) = answer(node)
        override fun declare(node: String) {
            declared += node
        }
    }

    @BeforeEach
    @AfterEach
    fun reset() {
        ServiceManager.unregister(PermissionChecker::class.java)
    }

    @Test
    fun `denies when no checker is registered and no fallback is given`() {
        assertFalse(LuxPermissions.has(sender, "luxapi.admin"))
    }

    @Test
    fun `uses the supplied fallback when no checker is registered`() {
        assertTrue(LuxPermissions.has(sender, "luxapi.admin") { true })
        assertFalse(LuxPermissions.has(sender, "luxapi.admin") { false })
    }

    @Test
    fun `returns the registered checker's answer and ignores the fallback`() {
        ServiceManager.register(PermissionChecker::class.java, RecordingChecker { it == "luxapi.allowed" })

        assertTrue(LuxPermissions.has(sender, "luxapi.allowed") { false })
        assertFalse(LuxPermissions.has(sender, "luxapi.denied") { true })
    }

    @Test
    fun `denies a blank node even when the checker would allow it`() {
        ServiceManager.register(PermissionChecker::class.java, RecordingChecker { true })

        assertFalse(LuxPermissions.has(sender, ""))
        assertFalse(LuxPermissions.has(sender, "   "))
    }

    @Test
    fun `denies when the checker throws`() {
        ServiceManager.register(PermissionChecker::class.java, RecordingChecker { throw IllegalStateException("backend down") })

        assertFalse(LuxPermissions.has(sender, "luxapi.admin") { true })
    }

    @Test
    fun `declare forwards the node to the registered checker`() {
        val checker = RecordingChecker { true }
        ServiceManager.register(PermissionChecker::class.java, checker)

        LuxPermissions.declare("luxapi.command.reload")

        assertEquals(listOf("luxapi.command.reload"), checker.declared)
    }

    @Test
    fun `declare without a checker is a harmless no-op`() {
        LuxPermissions.declare("luxapi.command.reload")
    }
}
