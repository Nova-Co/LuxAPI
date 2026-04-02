package com.novaco.luxapi.fabric.event

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.fabric.player.FabricLuxPlayer
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * Acts as a bridge between native Fabric server events and the cross-platform LuxAPI EventBus.
 * Responsible for intercepting platform-specific packets and dispatching them as LuxEvents.
 */
object FabricEventBridge {

    /**
     * Registers all native Fabric event listeners and translates them into LuxAPI events.
     * This includes player connection state changes and chat message interception.
     */
    fun register() {
        // --- Player Join Event ---
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val luxPlayer = FabricLuxPlayer(handler.player)
            EventBus.fire(PlayerJoinEvent(luxPlayer))
        }

        // --- Player Quit Event ---
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            val luxPlayer = FabricLuxPlayer(handler.player)
            EventBus.fire(PlayerQuitEvent(luxPlayer))
        }

        // --- Player Chat Event ---
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register { message, sender, _ ->
            val luxPlayer = FabricLuxPlayer(sender)
            val rawMessage = message.signedContent()

            // Collect all online players to populate the recipients list
            val recipients = sender.server.playerList.players.map { FabricLuxPlayer(it) as LuxPlayer }.toMutableSet()

            // Dispatch the cross-platform event
            val event = PlayerChatEvent(luxPlayer, rawMessage, "<%player_name%> %message%", recipients)
            EventBus.fire(event)

            // If a plugin cancelled the event, stop processing and block the vanilla chat
            if (event.isCancelled) {
                return@register false
            }

            // Apply placeholders and formatting
            val renderedText = PlaceholderManager.replace(luxPlayer, event.getRenderedMessage())
            val component = Component.literal(renderedText)

            // Broadcast the formatted message manually to the designated recipients
            event.recipients.forEach { target ->
                val targetServerPlayer = target.parent as ServerPlayer
                targetServerPlayer.sendSystemMessage(component)
            }

            // Always return false to suppress the default vanilla chat broadcast,
            // as we have manually handled the delivery above.
            false
        }
    }
}