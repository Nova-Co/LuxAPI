package com.novaco.luxapi.core.command

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.core.text.TextUtils
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.player.Player
import java.util.UUID

/**
 * A wrapper class that adapts Minecraft's native CommandSourceStack
 * into the cross-platform LuxAPI CommandSender interface.
 */
class MinecraftCommandSender(val source: CommandSourceStack) : CommandSender {

    /**
     * The display name of the command sender.
     */
    override val name: String
        get() = source.textName

    /**
     * The unique identifier of the command sender.
     * Returns null if the sender is the server console or a command block.
     */
    override val uniqueId: UUID?
        get() = source.entity?.uuid

    /**
     * Sends a formatted message to the command sender.
     * Automatically translates legacy color codes.
     *
     * @param message The raw string message with color codes.
     */
    override fun sendMessage(message: String) {
        source.sendSystemMessage(TextUtils.format(message))
    }

    /**
     * Checks if the sender has the required permission.
     *
     * @param permission The permission node string to check.
     * @return True if the sender has permission, false otherwise.
     */
    override fun hasPermission(permission: String): Boolean {
        return source.hasPermission(2)
    }

    /**
     * Determines whether the sender is a physical player in the world.
     *
     * @return True if the sender is a Player entity, false otherwise.
     */
    override fun isPlayer(): Boolean {
        return source.entity is Player
    }

    /**
     * Casts and returns the underlying native Player object.
     *
     * @return The Player object, or null if the sender is not a player.
     */
    fun getPlayer(): Player? {
        return source.entity as? Player
    }
}