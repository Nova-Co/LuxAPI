package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Interface for external modules to register custom placeholders.
 * Example: A Cobblemon module could provide "%cobblemon_party_size%".
 */
interface PlaceholderProvider {

    /**
     * The unique identifier for this provider.
     * Example: "player" for %player_name%.
     */
    fun identifier(): String

    /**
     * Processes a placeholder request for a specific player.
     * @param player The player context.
     * @param params The parameters after the identifier (e.g., "name" in %player_name%).
     * @return The replacement string, or null if not handled.
     */
    fun onPlaceholderRequest(player: LuxPlayer?, params: String): String?
}