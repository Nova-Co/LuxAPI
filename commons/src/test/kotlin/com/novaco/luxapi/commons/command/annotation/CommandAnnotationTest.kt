package com.novaco.luxapi.commons.command.annotation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * A dummy command class used to verify annotation reflection.
 */
@Command(name = "economy", aliases = ["eco", "money"], permission = "luxapi.eco")
class DummyEconomyCommand {

    @SubCommand(name = "give", permission = "luxapi.eco.give")
    fun giveMoney() {
        // Dummy method
    }

    fun notACommand() {
        // This method lacks an annotation and should be ignored
    }
}

class CommandAnnotationTest {

    @Test
    fun `test class level command annotation parsing`() {
        // Access the Java class directly to use standard getAnnotation
        val clazz = DummyEconomyCommand::class.java
        val commandAnno = clazz.getAnnotation(Command::class.java)

        assertNotNull(commandAnno, "Class must have the @Command annotation.")
        assertEquals("economy", commandAnno.name)
        assertTrue(commandAnno.aliases.contains("eco"), "Aliases must contain 'eco'.")
        assertEquals("luxapi.eco", commandAnno.permission)
    }

    @Test
    fun `test method level subcommand annotation parsing`() {
        val clazz = DummyEconomyCommand::class.java

        // Use Java's declaredMethods instead of Kotlin's declaredFunctions
        val methods = clazz.declaredMethods
        val giveMethod = methods.find { it.name == "giveMoney" }

        assertNotNull(giveMethod, "Method should exist.")

        val subCommandAnno = giveMethod?.getAnnotation(SubCommand::class.java)
        assertNotNull(subCommandAnno, "Method must have the @SubCommand annotation.")
        assertEquals("give", subCommandAnno?.name)

        // Verify functions without annotations are safely ignored
        val ignoredMethod = methods.find { it.name == "notACommand" }
        assertNull(ignoredMethod?.getAnnotation(SubCommand::class.java))
    }
}