package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.regex.Pattern

/**
 * The central registry for all placeholders within LuxAPI.
 * Scans strings for patterns like %identifier_params% and replaces them dynamically.
 */
object PlaceholderManager {

    private val providers = mutableMapOf<String, PlaceholderProvider>()
    private val PATTERN = Pattern.compile("%([^%]+)%")

    /**
     * Registers a new placeholder provider into the system.
     */
    fun register(provider: PlaceholderProvider) {
        providers[provider.identifier().lowercase()] = provider
    }

    /**
     * Replaces all recognized placeholders in a string with their actual values.
     *
     * @param player The context player requesting the replacement.
     * @param text The raw string containing placeholders.
     * @return The formatted string.
     */
    fun replace(player: LuxPlayer?, text: String): String {
        val matcher = PATTERN.matcher(text)
        val sb = StringBuilder()
        var lastEnd = 0

        while (matcher.find()) {
            sb.append(text, lastEnd, matcher.start())
            val content = matcher.group(1)

            val parts = content.split("_", limit = 2)
            val identifier = parts[0].lowercase()
            val params = parts.getOrNull(1) ?: ""
            val replacement = providers[identifier]?.onPlaceholderRequest(player, params)

            sb.append(replacement ?: matcher.group(0))
            lastEnd = matcher.end()
        }
        sb.append(text.substring(lastEnd))
        return sb.toString()
    }
}