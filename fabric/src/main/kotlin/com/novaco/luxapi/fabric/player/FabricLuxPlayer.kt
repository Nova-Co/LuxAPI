package com.novaco.luxapi.fabric.player

import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

/**
 * Fabric implementation of LuxPlayer wrapping Minecraft's native ServerPlayer object.
 * * @param serverPlayer The target native ServerPlayer instance.
 */
class FabricLuxPlayer(private var serverPlayer: ServerPlayer) : LuxPlayer {

    override val name: String
        get() = serverPlayer.scoreboardName

    override val uniqueId: UUID
        get() = serverPlayer.uuid

    override val parent: Any
        get() = serverPlayer

    override val locale: String
        get() = serverPlayer.clientInformation().language()

    override val position: Vector3D
        get() = Vector3D(serverPlayer.x, serverPlayer.y, serverPlayer.z)

    /**
     * Re-assigns the inner native player reference upon world transits or respawns.
     * * @param player The newly instantiated ServerPlayer snapshot.
     */
    fun updateInstance(player: ServerPlayer) {
        this.serverPlayer = player
    }

    override fun sendMessage(message: String) {
        serverPlayer.sendSystemMessage(Component.literal(message))
    }

    override fun hasPermission(permission: String): Boolean {
        return true
    }

    override fun sendTitle(title: String, subtitle: String) {
    }

    override fun kick(reason: String) {
        serverPlayer.connection.disconnect(Component.literal(reason))
    }
}