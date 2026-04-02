package com.novaco.luxapi.neoforge.event

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.neoforge.player.NeoForgeLuxPlayer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

/**
 * Acts as a bridge between native NeoForge server events and the cross-platform LuxAPI EventBus.
 * Responsible for intercepting platform-specific events and dispatching them as LuxEvents.
 */
object NeoForgeEventBridge {

    /**
     * Hooks into the main NeoForge event bus to begin listening for platform events.
     */
    fun register() {
        NeoForge.EVENT_BUS.register(this)
    }

    /**
     * Intercepts the native NeoForge login event and fires a LuxAPI PlayerJoinEvent.
     */
    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity
        if (player is ServerPlayer) {
            val luxPlayer = NeoForgeLuxPlayer(player)
            EventBus.fire(PlayerJoinEvent(luxPlayer))
        }
    }

    /**
     * Intercepts the native NeoForge logout event and fires a LuxAPI PlayerQuitEvent.
     */
    @SubscribeEvent
    fun onPlayerQuit(event: PlayerEvent.PlayerLoggedOutEvent) {
        val player = event.entity
        if (player is ServerPlayer) {
            val luxPlayer = NeoForgeLuxPlayer(player)
            EventBus.fire(PlayerQuitEvent(luxPlayer))
        }
    }

    /**
     * Intercepts the native NeoForge chat event, allowing cross-platform plugins to
     * modify the format, cancel the message, or alter the recipients.
     */
    @SubscribeEvent
    fun onPlayerChat(event: ServerChatEvent) {
        val player = event.player
        val luxPlayer = NeoForgeLuxPlayer(player)
        val rawMessage = event.rawText

        // Collect all online players to populate the recipients list
        val recipients = player.server.playerList.players.map { NeoForgeLuxPlayer(it) as LuxPlayer }.toMutableSet()

        // Dispatch the cross-platform event
        val luxEvent = PlayerChatEvent(luxPlayer, rawMessage, "<%player_name%> %message%", recipients)
        EventBus.fire(luxEvent)

        // Cancel the vanilla event regardless, to handle custom broadcasting
        event.isCanceled = true

        if (!luxEvent.isCancelled) {
            // Apply placeholders and formatting
            val renderedText = PlaceholderManager.replace(luxPlayer, luxEvent.getRenderedMessage())
            val component = Component.literal(renderedText)

            // Broadcast the formatted message manually to the designated recipients
            luxEvent.recipients.forEach { target ->
                val targetServerPlayer = target.parent as ServerPlayer
                targetServerPlayer.sendSystemMessage(component)
            }
        }
    }
}