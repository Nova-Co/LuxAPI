package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Opt-in extension of [PlaceholderProvider] for a provider whose output can span multiple
 * lines (e.g. a stat block). Only used by [PlaceholderManager.replaceLines], for a line that
 * is *entirely* a single placeholder token (e.g. a GUI lore line or book page consisting of
 * just "%player_stats%") — inline placeholders mixed into a larger line still resolve through
 * the single-line [PlaceholderProvider.onPlaceholderRequest].
 */
interface MultiLinePlaceholderProvider : PlaceholderProvider {

    /**
     * Processes a placeholder request, returning the lines it expands into.
     * @param player The player context.
     * @param params The parameters after the identifier (e.g. "stats" in %player_stats%).
     * @return The replacement lines, or null if not handled (falls back to [PlaceholderProvider.onPlaceholderRequest]).
     */
    fun onMultiLinePlaceholderRequest(player: LuxPlayer?, params: String): List<String>?
}
