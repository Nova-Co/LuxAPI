package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * The default placeholder provider for resolving player-specific information.
 * This class handles placeholder requests under the "player" prefix, supplying basic
 * details such as the player's name and UUID, or defaulting to "Guest" for null players.
 */
class DefaultPlayerProvider : PlaceholderProvider {

    /**
     * Defines the root identifier for this placeholder provider.
     *
     * @return The string identifier "player".
     */
    override fun identifier(): String = "player"

    /**
     * Processes the incoming placeholder request and evaluates the provided parameters.
     * If the [player] is null, it immediately returns "Guest".
     * It supports resolving the "name" to the player's name and "uuid" to the player's unique ID.
     *
     * @param player The target [LuxPlayer] for whom the placeholder is being parsed, or null if unavailable.
     * @param params The specific variable requested (e.g., "name" or "uuid").
     * @return The resolved string value corresponding to the parameter, or null if the parameter is unsupported.
     */
    override fun onPlaceholderRequest(player: LuxPlayer?, params: String): String? {
        if (player == null) return "Guest"
        return when (params.lowercase()) {
            "name" -> player.name
            "uuid" -> player.uniqueId.toString()
            else -> null
        }
    }
}