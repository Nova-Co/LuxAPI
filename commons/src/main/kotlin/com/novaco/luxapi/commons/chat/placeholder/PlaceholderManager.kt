package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.regex.Pattern

/**
 * The central registry for all placeholders within LuxAPI.
 * Scans strings for patterns like %identifier_params% and replaces them.
 */
object PlaceholderManager {

    private val providers = mutableMapOf<String, PlaceholderProvider>()
    private val PATTERN = Pattern.compile("%([a-zA-Z0-9_]+)_([a-zA-Z0-9_]+)%")

    /**
     * Registers a new placeholder provider.
     */
    fun register(provider: PlaceholderProvider) {
        providers[provider.identifier().lowercase()] = provider
    }

    /**
     * Replaces all recognized placeholders in a string with their actual values.
     */
    fun replace(player: LuxPlayer?, text: String): String {
        val matcher = PATTERN.matcher(text)
        val sb = StringBuilder()
        var lastEnd = 0

        while (matcher.find()) {
            sb.append(text, lastEnd, matcher.start())
            val identifier = matcher.group(1).lowercase()
            val params = matcher.group(2)

            val replacement = providers[identifier]?.onPlaceholderRequest(player, params)
            sb.append(replacement ?: matcher.group(0))
            lastEnd = matcher.end()
        }
        sb.append(text.substring(lastEnd))
        return sb.toString()
    }
}