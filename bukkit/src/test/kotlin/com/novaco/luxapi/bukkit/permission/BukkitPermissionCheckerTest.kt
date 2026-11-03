package com.novaco.luxapi.bukkit.permission

import com.novaco.luxapi.commons.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class BukkitPermissionCheckerTest {

    private class FakeSender(private val granted: Set<String>) : CommandSender {
        override val name = "tester"
        override val uniqueId: UUID? = null
        override fun sendMessage(message: String) {}
        override fun hasPermission(permission: String) = permission in granted
    }

    private val checker = BukkitPermissionChecker()

    @Test
    fun `grants a node the sender holds`() {
        assertTrue(checker.has(FakeSender(setOf("luxapi.admin")), "luxapi.admin"))
    }

    @Test
    fun `denies a node the sender does not hold`() {
        assertFalse(checker.has(FakeSender(setOf("luxapi.admin")), "luxapi.other"))
    }
}
