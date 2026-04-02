package com.novaco.luxapi.fabric.event

import com.novaco.luxapi.commons.event.EventBus
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.fabric.player.FabricLuxPlayer
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

/**
 * Acts as a bridge between native Fabric server events and the cross-platform LuxAPI EventBus.
 */
object FabricEventBridge {

    /**
     * Registers all native Fabric event listeners and translates them into LuxAPI events.
     */
    fun register() {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val luxPlayer = FabricLuxPlayer(handler.player)
            EventBus.fire(PlayerJoinEvent(luxPlayer))
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            val luxPlayer = FabricLuxPlayer(handler.player)
            EventBus.fire(PlayerQuitEvent(luxPlayer))
        }
    }
}