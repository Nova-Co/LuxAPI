package com.novaco.luxapi.commons.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringExtensionsTest {

    @Test
    fun `test color code translation`() {
        val rawText = "&aHello &bWorld"
        // Assuming your extension is called colorize() and it replaces '&' with Minecraft's '§'
        val coloredText = rawText.colorize()

        assertEquals("§aHello §bWorld", coloredText, "Ampersands should be translated to section symbols")
    }

    @Test
    fun `test strip colors`() {
        val coloredText = "§cWarning! §ePay attention."
        // Assuming stripColors() removes the formatting
        val plainText = coloredText.stripColors()

        assertEquals("Warning! Pay attention.", plainText, "All color formatting should be removed")
    }
}