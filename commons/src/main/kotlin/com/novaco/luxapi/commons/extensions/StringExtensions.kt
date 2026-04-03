package com.novaco.luxapi.commons.extensions

import java.util.regex.Pattern

/**
 * Extension functions for standard Kotlin Strings to assist with
 * Minecraft text formatting and manipulation.
 */

private val HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})")

/**
 * Translates legacy ampersand color codes (e.g., &a, &l) into Minecraft's
 * internal section symbol format (§a, §l). Also supports hex codes (&#RRGGBB).
 *
 * @return The color-translated string.
 */
fun String.colorize(): String {
    var text = this

    // Process Hex Colors (e.g., &#FF0000 -> §x§F§F§0§0§0§0)
    val matcher = HEX_PATTERN.matcher(text)
    val buffer = StringBuffer()
    while (matcher.find()) {
        val hex = matcher.group(1)
        val replacement = StringBuilder("§x")
        for (char in hex.toCharArray()) {
            replacement.append('§').append(char)
        }
        matcher.appendReplacement(buffer, replacement.toString())
    }
    matcher.appendTail(buffer)
    text = buffer.toString()

    // Process standard legacy colors
    return text.replace("&", "§")
}

/**
 * Strips all legacy color codes and formatting from the string.
 * Useful for logging to the console or saving raw data to a database.
 *
 * @return The raw string without any formatting symbols.
 */
fun String.stripColors(): String {
    return this.replace(Regex("(?i)§[0-9A-FK-ORX]"), "")
}