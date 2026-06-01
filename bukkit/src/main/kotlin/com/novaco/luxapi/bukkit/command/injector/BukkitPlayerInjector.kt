package com.novaco.luxapi.bukkit.command.injector

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Platform-specific argument injector for Bukkit's native Player class.
 */
class BukkitPlayerInjector : ArgumentInjector<Player> {

    override val convertedClass: Class<Player> = Player::class.java

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