package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.player.PlayerManager

/**
 * An [ArgumentInjector] responsible for parsing a player's name from a command string
 * and resolving it into a [LuxPlayer] object.
 *
 * @param playerManager The service used to look up players by their name or UUID.
 */
class PlayerInjector(private val playerManager: PlayerManager) : ArgumentInjector<LuxPlayer> {

    /**
     * The target class type that this injector handles, which is [LuxPlayer].
     */
    override val convertedClass: Class<LuxPlayer> = LuxPlayer::class.java

    /**
     * Instantiates a [LuxPlayer] from the command arguments by looking up their name.
     *
     * @param sender The entity that executed the command.
     * @param args The array of string arguments from the command.
     * @param index The specific index in the args array to parse as a player name.
     * @return The resolved [LuxPlayer] object.
     * @throws CommandParseException if the player name argument is missing or if the player cannot be found.
     */
    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): LuxPlayer {
        val targetName = args.getOrNull(index)
            ?: throw CommandParseException("§cError: Please specify a player name.")

        return playerManager.getPlayer(targetName)
            ?: throw CommandParseException("§cError: Could not find player '$targetName'. They might be offline.")
    }
}