package com.novaco.luxapi.neoforge.event

import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.player.PlayerChatEvent
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.neoforge.LuxNeoForgeInitializer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

/**
 * Acts as a bridge between native NeoForge server events and the cross-platform LuxAPI EventBus.
 * Responsible for intercepting platform-specific packets and dispatching them as LuxEvents.
 */
object NeoForgeEventBridge {

    /**
     * Hooks into the main NeoForge event bus to begin listening for platform events.
     */
    fun register() {
        NeoForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity
        if (player is ServerPlayer) {
            LuxNeoForgeInitializer.playerManager?.let { manager ->
                val luxPlayer = manager.login(player)
                EventBus.fire(PlayerJoinEvent(luxPlayer))
            }
        }
    }

    @SubscribeEvent
    fun onPlayerQuit(event: PlayerEvent.PlayerLoggedOutEvent) {
        val player = event.entity
        if (player is ServerPlayer) {
            LuxNeoForgeInitializer.playerManager?.let { manager ->
                val uuid = player.uuid
                val luxPlayer = manager.getPlayer(uuid)

                if (luxPlayer != null) {
                    EventBus.fire(PlayerQuitEvent(luxPlayer))
                }
                manager.logout(uuid)
            }
        }
    }

    @SubscribeEvent
    fun onPlayerChat(event: ServerChatEvent) {
        val player = event.player
        val playerManager = LuxNeoForgeInitializer.playerManager ?: return
        val luxPlayer = playerManager.getPlayer(player.uuid) ?: return

        val rawMessage = event.rawText

        val recipients = player.server.playerList.players.mapNotNull {
            playerManager.getPlayer(it.uuid)
        }.toMutableSet()

        val luxEvent = PlayerChatEvent(luxPlayer, rawMessage, "<%player_name%> %message%", recipients)
        EventBus.fire(luxEvent)

        event.isCanceled = true

        if (!luxEvent.isCancelled) {
            val renderedText = PlaceholderManager.replace(luxPlayer, luxEvent.getRenderedMessage())
            val component = Component.literal(renderedText)

            luxEvent.recipients.forEach { target ->
                val targetServerPlayer = target.parent as ServerPlayer
                targetServerPlayer.sendSystemMessage(component)
            }
        }
    }
}