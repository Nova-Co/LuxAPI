package com.novaco.luxapi.commons.config.annotation

import com.novaco.luxapi.commons.config.ConfigService
import com.novaco.luxapi.commons.config.LuxConfig
import com.novaco.luxapi.commons.config.serializer.PatternTypeSerializer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File
import java.util.regex.Pattern

@ConfigSerializable
@Config("pattern.yml")
@ScalarSerializers([PatternTypeSerializer::class])
class PatternHolderConfig : LuxConfig() {
    var filter: Pattern = Pattern.compile("default")
}

class ScalarSerializersTest {

    @Test
    fun `class-scoped serializer is used without touching the global registry`(@TempDir tempDir: File) {
        val config = ConfigService.load(PatternHolderConfig::class.java, tempDir)
        config.filter = Pattern.compile("^lux-[0-9]+$")
        config.save()

        val reloaded = ConfigService.load(PatternHolderConfig::class.java, tempDir)

        assertEquals("^lux-[0-9]+$", reloaded.filter.pattern())
        assertTrue(reloaded.filter.matcher("lux-42").matches())
    }
}
