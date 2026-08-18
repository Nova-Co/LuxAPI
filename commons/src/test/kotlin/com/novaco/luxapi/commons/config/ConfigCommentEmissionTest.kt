package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File

@ConfigSerializable
@Config("commented.yml")
@Comment("Top of the file.\nSecond header line.")
class CommentedTestConfig : LuxConfig() {

    @Comment("Explains the display name field.")
    var displayName: String = "Lux"

    // No @Comment here — must not get a stray comment line.
    var maxPlayers: Int = 100
}

class ConfigCommentEmissionTest {

    @Test
    fun `class comment becomes a real file header and field comments land above their keys`(@TempDir tempDir: File) {
        val config = ConfigService.load(CommentedTestConfig::class.java, tempDir)
        config.save()

        val lines = File(tempDir, "commented.yml").readLines()

        // Class-level @Comment (multi-line) is a real document header at the top of the file.
        assertEquals("# Top of the file.", lines[0])
        assertEquals("# Second header line.", lines[1])

        // Field-level @Comment sits directly above its actual (kebab-cased) key.
        val displayNameCommentIndex = lines.indexOf("# Explains the display name field.")
        val displayNameKeyIndex = lines.indexOfFirst { it.startsWith("display-name:") }
        assertTrue(displayNameCommentIndex >= 0 && displayNameKeyIndex == displayNameCommentIndex + 1)

        // The uncommented field's key has no comment line immediately above it.
        val maxPlayersKeyIndex = lines.indexOfFirst { it.startsWith("max-players:") }
        assertFalse(lines[maxPlayersKeyIndex - 1].startsWith("#"))

        // No stray camelCase duplicate keys from the earlier naming-scheme bug.
        assertTrue(lines.none { it.startsWith("displayName:") || it.startsWith("maxPlayers:") })
    }
}
