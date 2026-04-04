package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * A dummy configuration class to test annotation processing.
 * Must extend LuxConfig to mimic real implementation.
 */
@Config("server.yml")
@Comment("Main server configuration file.")
class DummyServerConfig : LuxConfig() {

    @Comment("The display name of the server.")
    var serverName: String = "LuxServer"

    // This field has no comment, which is perfectly valid
    var maxPlayers: Int = 100
}

class ConfigAnnotationTest {

    @Test
    fun `test class level config and comment annotations`() {
        val configClass = DummyServerConfig::class.java

        // Verify that the class has the annotations
        assertTrue(configClass.isAnnotationPresent(Config::class.java), "The config class should have the @Config annotation.")
        assertTrue(configClass.isAnnotationPresent(Comment::class.java), "The config class should have the @Comment annotation.")

        // Verify the values inside the annotations
        val configAnno = configClass.getAnnotation(Config::class.java)
        assertEquals("server.yml", configAnno.path, "The @Config path should correctly match the defined string.")

        val commentAnno = configClass.getAnnotation(Comment::class.java)
        assertEquals("Main server configuration file.", commentAnno.value, "The class-level @Comment value should match.")
    }

    @Test
    fun `test field level comment annotations`() {
        val configClass = DummyServerConfig::class.java

        // Test a field that HAS a comment
        val nameField = configClass.getDeclaredField("serverName")
        assertTrue(nameField.isAnnotationPresent(Comment::class.java), "The serverName field should have a @Comment annotation.")
        assertEquals("The display name of the server.", nameField.getAnnotation(Comment::class.java).value)

        // Test a field that does NOT have a comment
        val maxPlayersField = configClass.getDeclaredField("maxPlayers")
        assertFalse(maxPlayersField.isAnnotationPresent(Comment::class.java), "Fields without the @Comment annotation should return false safely.")
    }
}