package com.novaco.luxapi.core.text

import net.minecraft.network.chat.Component

/**
 * A utility class for handling Minecraft text components and color codes.
 */
object TextUtils {

    /**
     * Converts a string with legacy color codes (&) into a colored Component.
     */
    fun format(text: String): Component {
        val colorized = text.replace("&", "§")
        return Component.literal(colorized)
    }

    /**
     * Helper method for converting a list of strings to components.
     */
    fun formatList(lines: List<String>): List<Component> {
        return lines.map { format(it) }
    }
}