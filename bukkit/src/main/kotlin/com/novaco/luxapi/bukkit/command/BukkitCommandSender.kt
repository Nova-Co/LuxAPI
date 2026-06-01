package com.novaco.luxapi.bukkit.command

import com.novaco.luxapi.commons.command.sender.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Bukkit implementation of the LuxAPI [CommandSender] interface.
 * Wraps a native Bukkit [org.bukkit.command.CommandSender] to bridge platform-agnostic operations,
 * allowing core command logic to seamlessly interact with Bukkit-specific entities.
 *
 * @property sender The underlying native Bukkit command sender (e.g., a Player or Console).
 */
class BukkitCommandSender(private val sender: org.bukkit.command.CommandSender) : CommandSender {

    /**
     * Retrieves the name of the entity executing the command.
     * For players, this returns their username; for the console, it typically returns "CONSOLE".
     */
    override val name: String
        get() = sender.name

    /**
     * Retrieves the unique identifier of the command sender.
     * Returns the [UUID] if the sender is an actual [Player], otherwise returns null
     * (e.g., when the command is executed by the server console).
     */
    override val uniqueId: UUID?
        get() = if (sender is Player) sender.uniqueId else null

    /**
     * Sends a raw text message directly to the command sender.
     *
     * @param message The string content to be displayed in the sender's chat or console output.
     */
    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    /**
     * Verifies whether the command sender possesses the specified permission node.
     *
     * @param permission The permission node string to check (e.g., "luxapi.admin").
     * @return true if the sender has been granted the permission or holds server operator status, false otherwise.
     */
    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}