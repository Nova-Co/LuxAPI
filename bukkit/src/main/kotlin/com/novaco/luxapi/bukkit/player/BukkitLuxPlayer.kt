package com.novaco.luxapi.bukkit.player

import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Bukkit implementation of LuxPlayer wrapping a native [Player] instance.
 *
 * @param bukkitPlayer The target native Bukkit player.
 */
class BukkitLuxPlayer(private val bukkitPlayer: Player) : LuxPlayer {

    override val name: String
        get() = bukkitPlayer.name

    override val uniqueId: UUID
        get() = bukkitPlayer.uniqueId

    override val parent: Any
        get() = bukkitPlayer

    override val locale: String
        get() = bukkitPlayer.locale.toString()

    override val position: Vector3D
        get() = bukkitPlayer.location.let { Vector3D(it.x, it.y, it.z) }

    override fun sendMessage(message: String) {
        bukkitPlayer.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return bukkitPlayer.hasPermission(permission)
    }

    override fun sendTitle(title: String, subtitle: String) {
        bukkitPlayer.sendTitle(title, subtitle, 10, 70, 20)
    }

    override fun kick(reason: String) {
        bukkitPlayer.kickPlayer(reason)
    }
}
