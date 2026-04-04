package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.data.TimeGateManager
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.time.TimeFormatUtils
import java.util.UUID

/**
 * Provides placeholder integration for the TimeGate system.
 * Allows parsing variables like %cooldown_heal% into human-readable time.
 *
 * @property timeGate The global TimeGateManager instance tracking UUIDs.
 */
class CooldownPlaceholderProvider(private val timeGate: TimeGateManager<UUID>) : PlaceholderProvider {

    /**
     * Returns the identifier prefix for this placeholder provider.
     * For example, returning "cooldown" will handle %cooldown_<params>%.
     *
     * @return The placeholder identifier string.
     */
    override fun identifier(): String {
        return "cooldown"
    }

    /**
     * Processes the placeholder request and returns the formatted remaining time.
     *
     * @param player The player requesting the placeholder, used for i18n translation.
     * @param params The specific cooldown category requested (e.g., "heal").
     * @return The formatted time string, or null if the player is invalid.
     */
    override fun onPlaceholderRequest(player: LuxPlayer?, params: String): String? {
        val uuid = player?.uniqueId ?: return null
        val remainingMillis = timeGate.getRemainingTime(category = params, key = uuid)
        return TimeFormatUtils.formatDuration(remainingMillis, player)
    }
}