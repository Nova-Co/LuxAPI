package com.novaco.luxapi.commons.config.type

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A config-serializable text progress bar — renders a filled/empty character run from a
 * 0.0-1.0 percentage, for chat, scoreboards, or lore lines.
 */
@ConfigSerializable
class ProgressBar {

    var length: Int = 10
    var filledChar: Char = '█'
    var emptyChar: Char = '▒'
    var filledPrefix: String = ""
    var emptyPrefix: String = ""

    fun render(percentage: Double): String {
        val clamped = percentage.coerceIn(0.0, 1.0)
        val filled = (length * clamped).toInt().coerceIn(0, length)
        val empty = length - filled

        return buildString {
            append(filledPrefix)
            repeat(filled) { append(filledChar) }
            append(emptyPrefix)
            repeat(empty) { append(emptyChar) }
        }
    }

    fun render(progress: Double, total: Double): String {
        if (total <= 0.0) return render(0.0)
        return render(progress / total)
    }
}
