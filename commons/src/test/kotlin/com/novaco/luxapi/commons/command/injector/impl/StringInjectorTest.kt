package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * A dummy CommandSender implementation used exclusively to fulfill the
 * instantiate method's signature without requiring a real Minecraft server.
 */
class DummyCommandSender : CommandSender {
    override val name: String = "TestConsole"
    override val uniqueId: UUID = UUID.randomUUID()
    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = true
    override fun isPlayer(): Boolean = false
}

class StringInjectorTest {

    private lateinit var stringInjector: StringInjector
    private lateinit var integerInjector: IntegerInjector
    private lateinit var dummySender: DummyCommandSender

    @BeforeEach
    fun setup() {
        stringInjector = StringInjector()
        integerInjector = IntegerInjector()
        dummySender = DummyCommandSender()
    }

    @Test
    fun `test string argument parsing successfully`() {
        val args = arrayOf("Cobblemon", "150")

        // Target index 0 ("Cobblemon")
        val parsed = stringInjector.instantiate(dummySender, args, 0)

        assertEquals("Cobblemon", parsed, "String injector should return the exact string at the given index.")
    }

    @Test
    fun `test integer argument parsing successfully`() {
        val args = arrayOf("Cobblemon", "150")

        // Target index 1 ("150")
        val parsed = integerInjector.instantiate(dummySender, args, 1)

        assertEquals(150, parsed, "Integer injector should correctly parse numeric strings.")
    }

    @Test
    fun `test integer argument parsing fails gracefully on invalid string`() {
        val args = arrayOf("NotANumber")

        // Expecting your custom CommandParseException to be thrown
        assertThrows(CommandParseException::class.java, {
            integerInjector.instantiate(dummySender, args, 0)
        }, "Injector should throw a CommandParseException when parsing a non-numeric string.")
    }

    @Test
    fun `test out of bounds index throws exception`() {
        val args = emptyArray<String>()

        // Both injectors should throw an exception if the index does not exist
        assertThrows(CommandParseException::class.java, {
            stringInjector.instantiate(dummySender, args, 0)
        }, "Missing string argument should throw a CommandParseException.")

        assertThrows(CommandParseException::class.java, {
            integerInjector.instantiate(dummySender, args, 0)
        }, "Missing integer argument should throw a CommandParseException.")
    }
}