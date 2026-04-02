package com.novaco.luxapi.neoforge.player

import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * The NeoForge implementation of the cross-platform LuxPlayer.
 * Wraps a native NeoForge ServerPlayer.
 */
class NeoForgeLuxPlayer(private val serverPlayer: ServerPlayer) : LuxPlayer {

    override val name: String get() = serverPlayer.scoreboardName
    override val uniqueId: UUID get() = serverPlayer.uuid
    override val parent: Any get() = serverPlayer

    override fun sendMessage(message: String) {
        serverPlayer.sendSystemMessage(Component.literal(message))
    }

    override fun hasPermission(permission: String): Boolean {
        // TODO: Implement actual permission check using NeoForge's permission API later.
        return true
    }

    override fun sendTitle(title: String, subtitle: String) {
        // Implementation for titles can be added here
    }

    override fun kick(reason: String) {
        serverPlayer.connection.disconnect(Component.literal(reason))
    }
}