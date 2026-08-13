package com.novaco.luxapi.bukkit.event

import com.novaco.luxapi.bukkit.player.BukkitPlayerManager
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent as BukkitPlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent as BukkitPlayerQuitEvent
import org.bukkit.plugin.Plugin

/**
 * Bridges native Bukkit player events into the cross-platform LuxAPI [EventBus],
 * mirroring `FabricEventBridge`/`NeoForgeEventBridge`'s role on the modded platforms.
 *
 * @param plugin The owning plugin instance, required to register listeners with Bukkit.
 * @param playerManager The [BukkitPlayerManager] used to resolve/cache [com.novaco.luxapi.commons.player.LuxPlayer] wrappers.
 */
class BukkitEventBridge(private val plugin: Plugin, private val playerManager: BukkitPlayerManager) : Listener {

    /**
     * Registers this bridge with the Bukkit plugin manager.
     */
    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: BukkitPlayerJoinEvent) {
        val luxPlayer = playerManager.login(event.player)
        EventBus.fire(PlayerJoinEvent(luxPlayer))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: BukkitPlayerQuitEvent) {
        val luxPlayer = playerManager.getPlayer(event.player.uniqueId)
        if (luxPlayer != null) {
            EventBus.fire(PlayerQuitEvent(luxPlayer))
        }
        playerManager.logout(event.player.uniqueId)
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val luxPlayer = playerManager.getPlayer(event.player.uniqueId) ?: return
        val recipients = event.recipients.mapNotNull { playerManager.getPlayer(it.uniqueId) }.toMutableSet()

        val luxEvent = PlayerChatEvent(luxPlayer, event.message, "<%player_name%> %message%", recipients)
        EventBus.fire(luxEvent)

        // This bridge fully takes over delivery (same pattern as FabricEventBridge) so
        // format/recipient changes made by listeners are honored exactly, not merged with
        // Bukkit's own %1$s/%2$s format-string mechanism.
        event.isCancelled = true

        if (luxEvent.isCancelled) return

        val renderedText = PlaceholderManager.replace(luxPlayer, luxEvent.getRenderedMessage())
        luxEvent.recipients.forEach { recipient ->
            (recipient.parent as? org.bukkit.entity.Player)?.sendMessage(renderedText)
        }
    }
}
