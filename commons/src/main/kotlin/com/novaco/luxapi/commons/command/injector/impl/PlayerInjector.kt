package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.player.PlayerManager

class PlayerInjector(private val playerManager: PlayerManager) : ArgumentInjector<LuxPlayer> {

    override val convertedClass: Class<LuxPlayer> = LuxPlayer::class.java

    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): LuxPlayer {
        val targetName = args.getOrNull(index)
            ?: throw CommandParseException("§cError: Please specify a player name.")

        return playerManager.getPlayer(targetName)
            ?: throw CommandParseException("§cError: Could not find player '$targetName'. They might be offline.")
    }
}