package com.novaco.luxapi.bukkit.command

import com.novaco.luxapi.commons.command.sender.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Bukkit implementation of the LuxAPI CommandSender interface.
 * Wraps a native Bukkit CommandSender to bridge platform-agnostic operations.
 */
class BukkitCommandSender(private val sender: org.bukkit.command.CommandSender) : CommandSender {

    override val name: String
        get() = sender.name

    override val uniqueId: UUID?
        get() = if (sender is Player) sender.uniqueId else null

    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}