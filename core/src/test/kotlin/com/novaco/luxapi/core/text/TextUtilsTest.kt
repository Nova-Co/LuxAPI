package com.novaco.luxapi.core.text

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class TextUtilsTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            // Bootstraps Minecraft's internal registries so Component.literal() works properly in a test environment.
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test basic color formatting`() {
        val raw = "&cError: &fSomething went wrong"
        val component = TextUtils.format(raw)

        // The & should be replaced with the section sign §
        val expectedText = "§cError: §fSomething went wrong"
        assertEquals(expectedText, component.string, "The legacy ampersand codes must be correctly converted to section signs.")
    }

    @Test
    fun `test format list logic`() {
        val rawList = listOf("&aLine 1", "&bLine 2")
        val components = TextUtils.formatList(rawList)

        assertEquals(2, components.size, "List should retain its size.")
        assertEquals("§aLine 1", components[0].string)
        assertEquals("§bLine 2", components[1].string)
    }

    @Test
    fun `test formatting string without color codes remains unchanged`() {
        val raw = "Plain text message"
        val component = TextUtils.format(raw)

        assertEquals(raw, component.string, "Strings without ampersands should render exactly as input.")
    }
}