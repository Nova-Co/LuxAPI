package com.novaco.luxapi.commons.extensions

import java.util.regex.Pattern

/**
 * Extension functions for standard Kotlin Strings to assist with
 * Minecraft text formatting and manipulation.
 */

private val HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})")
private val SECTION_FORMAT_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]")
private val AMPERSAND_CODE_PATTERN = Pattern.compile("(?i)&([0-9A-FK-ORX])")

/**
 * Translates legacy ampersand color codes (e.g., &a, &l) into Minecraft's
 * internal section symbol format (§a, §l). Also supports hex codes (&#RRGGBB).
 *
 * Only an `&` immediately followed by a recognized format character is converted —
 * validated and transformed in the same pass (CERT IDS11-J) — so a stray `&` (e.g. "Fish
 * & Chips") is left untouched instead of silently becoming an unrecognized `§` sequence,
 * and no caller can be tricked into validating the pre-image while displaying the transformed
 * (potentially code-bearing) result.
 *
 * @return The color-translated string.
 */
fun String.colorize(): String {
    var text = this

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

    return AMPERSAND_CODE_PATTERN.matcher(text).replaceAll("§$1")
}

/**
 * Strips all legacy color codes and formatting from the string.
 * Useful for logging to the console or saving raw data to a database.
 *
 * @return The raw string without any formatting symbols.
 */
fun String.stripColors(): String {
    return SECTION_FORMAT_PATTERN.matcher(this).replaceAll("")
}