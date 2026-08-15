package com.novaco.luxapi.core.text

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

/**
 * A utility class for handling Minecraft text components and color codes.
 */
object TextUtils {

    private val HEX_TOKEN = Regex("&#([A-Fa-f0-9]{6})")
    private val LEGACY_CODE = Regex("(?i)[&§][0-9A-FK-OR]")

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

    /**
     * Removes both legacy (&/§) and hex (&#RRGGBB) color codes, leaving plain text.
     */
    fun strip(text: String): String {
        return LEGACY_CODE.replace(HEX_TOKEN.replace(text, ""), "")
    }

    /**
     * Converts a string containing `&#RRGGBB` hex tokens into a styled Component.
     * Text outside hex tokens falls back to legacy (&) formatting.
     */
    fun formatHex(text: String): Component {
        val matches = HEX_TOKEN.findAll(text).toList()
        if (matches.isEmpty()) return format(text)

        val root: MutableComponent = Component.empty()
        var cursor = 0
        var color: TextColor? = null

        for (match in matches) {
            if (match.range.first > cursor) {
                val segment = Component.literal(text.substring(cursor, match.range.first))
                root.append(if (color != null) segment.withStyle(Style.EMPTY.withColor(color)) else segment)
            }
            color = TextColor.fromRgb(match.groupValues[1].toInt(16))
            cursor = match.range.last + 1
        }
        if (cursor < text.length) {
            val segment = Component.literal(text.substring(cursor))
            root.append(segment.withStyle(Style.EMPTY.withColor(color)))
        }
        return root
    }

    /**
     * Renders [text] as a smooth per-character color gradient from [from] to [to].
     */
    fun gradient(text: String, from: TextColor, to: TextColor): Component {
        val root: MutableComponent = Component.empty()
        val lastIndex = (text.length - 1).coerceAtLeast(1)

        text.forEachIndexed { index, char ->
            val ratio = index.toDouble() / lastIndex
            val color = TextColor.fromRgb(lerpColor(from.value, to.value, ratio))
            root.append(Component.literal(char.toString()).withStyle(Style.EMPTY.withColor(color)))
        }
        return root
    }

    private fun lerpChannel(a: Int, b: Int, t: Double): Int {
        return (a + (b - a) * t).toInt().coerceIn(0, 255)
    }

    private fun lerpColor(a: Int, b: Int, t: Double): Int {
        val r = lerpChannel((a shr 16) and 0xFF, (b shr 16) and 0xFF, t)
        val g = lerpChannel((a shr 8) and 0xFF, (b shr 8) and 0xFF, t)
        val bl = lerpChannel(a and 0xFF, b and 0xFF, t)
        return (r shl 16) or (g shl 8) or bl
    }
}