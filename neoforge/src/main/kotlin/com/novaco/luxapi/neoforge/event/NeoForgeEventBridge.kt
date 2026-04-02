package com.novaco.luxapi.neoforge.event

import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.neoforge.player.NeoForgeLuxPlayer
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.PlayerEvent

/**
 * Acts as a bridge between native NeoForge server events and the cross-platform LuxAPI EventBus.
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
}