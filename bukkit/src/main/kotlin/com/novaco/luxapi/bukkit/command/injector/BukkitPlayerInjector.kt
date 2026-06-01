package com.novaco.luxapi.bukkit.command.injector

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Platform-specific argument injector for Bukkit's native [Player] class.
 * Responsible for parsing a string argument from a command and converting it into
 * an online Bukkit player instance.
 */
class BukkitPlayerInjector : ArgumentInjector<Player> {

    /**
     * The target class type that this injector is responsible for handling.
     */
    override val convertedClass: Class<Player> = Player::class.java

    /**
     * Instantiates and returns a Bukkit [Player] based on the provided command arguments.
     * Extracts the player's name from the arguments array at the specified index,
     * and attempts to find an exact match for an online player on the server.
     *
     * @param sender The entity executing the command.
     * @param args The full array of string arguments provided in the command.
     * @param index The current index of the argument being parsed.
     * @return The online Bukkit [Player] corresponding to the provided name.
     * @throws CommandParseException If no name is provided, or if the player cannot be found or is offline.
     */
    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): Player {
        val targetName = args.getOrNull(index)
            ?: throw CommandParseException("§cError: Please specify a player name.")

        val player = Bukkit.getPlayerExact(targetName)
        if (player == null || !player.isOnline) {
            throw CommandParseException("§cError: Could not find player '$targetName'. They might be offline.")
        }

        return player
    }
}