package com.novaco.luxapi.fabric.event

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.fabric.LuxFabricInitializer
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
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            LuxFabricInitializer.playerManager?.let { manager ->
                val luxPlayer = manager.login(handler.player)
                EventBus.fire(PlayerJoinEvent(luxPlayer))
            }
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            LuxFabricInitializer.playerManager?.let { manager ->
                val uuid = handler.player.uuid
                val luxPlayer = manager.getPlayer(uuid)
                if (luxPlayer != null) {
                    EventBus.fire(PlayerQuitEvent(luxPlayer))
                }
                manager.logout(uuid)
            }
        }

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register { message, sender, _ ->
            val playerManager = LuxFabricInitializer.playerManager ?: return@register false
            val luxPlayer = playerManager.getPlayer(sender.uuid) ?: return@register false
            val rawMessage = message.signedContent()

            val recipients = sender.server.playerList.players.mapNotNull {
                playerManager.getPlayer(it.uuid)
            }.toMutableSet()

            val event = PlayerChatEvent(luxPlayer, rawMessage, "<%player_name%> %message%", recipients)
            EventBus.fire(event)

            if (event.isCancelled) {
                return@register false
            }

            val renderedText = PlaceholderManager.replace(luxPlayer, event.getRenderedMessage())
            val component = Component.literal(renderedText)

            event.recipients.forEach { target ->
                val targetServerPlayer = target.parent as ServerPlayer
                targetServerPlayer.sendSystemMessage(component)
            }

            false
        }
    }
}