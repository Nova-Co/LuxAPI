package com.novaco.luxapi.bukkit.placeholder

import com.novaco.luxapi.bukkit.player.BukkitPlayerManager
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

/**
 * Soft-dependency bridge exposing LuxAPI's own [PlaceholderManager] registry as a
 * PlaceholderAPI expansion (`%lux_<identifier>_<params>%`), so placeholders registered
 * through LuxAPI's platform-agnostic [com.novaco.luxapi.commons.chat.placeholder.PlaceholderProvider]
 * work in any other plugin that resolves via PlaceholderAPI, not just LuxAPI's own text pipeline.
 *
 * Only takes effect if the consumer calls [register] — PlaceholderAPI presence is never assumed.
 */
class BukkitPlaceholderExpansion(private val playerManager: BukkitPlayerManager) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "lux"
    override fun getAuthor(): String = "LuxAPI"
    override fun getVersion(): String = "1.0"
    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        val luxPlayer = player?.let { playerManager.getPlayer(it.uniqueId) }
        val token = "%$params%"
        val result = PlaceholderManager.replace(luxPlayer, token)
        return if (result == token) null else result
    }

    companion object {
        /**
         * Registers this expansion with PlaceholderAPI, if it's installed on the server.
         * No-op (returns false) when PlaceholderAPI isn't present — safe to call unconditionally
         * from [com.novaco.luxapi.bukkit.LuxBukkitBridge.initialize].
         */
        fun register(playerManager: BukkitPlayerManager): Boolean {
            if (org.bukkit.Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return false
            return BukkitPlaceholderExpansion(playerManager).register()
        }
    }
}
