package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.CompletingInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.player.OfflinePlayer
import com.novaco.luxapi.commons.player.PlayerLookupService
import com.novaco.luxapi.commons.player.PlayerManager

/**
 * An [ArgumentInjector] that resolves a player's name into an [OfflinePlayer], succeeding
 * whether or not the target is currently online. Prefer [PlayerInjector] when the command
 * only makes sense for an online player (e.g. it needs to send them a message directly).
 * Also a [CompletingInjector]; suggestions only cover currently-online players since
 * [lookupService] has no enumeration, so an offline target still resolves on submit even
 * though tab-complete won't surface it.
 *
 * @param playerManager Checked first, so a currently-online player always resolves even if
 * they haven't been recorded in [lookupService] yet.
 * @param lookupService Cache of previously-seen name/UUID pairs, used when the target isn't online.
 */
class OfflinePlayerInjector(
    private val playerManager: PlayerManager,
    private val lookupService: PlayerLookupService
) : CompletingInjector<OfflinePlayer> {

    override val convertedClass: Class<OfflinePlayer> = OfflinePlayer::class.java

    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): OfflinePlayer {
        val targetName = args.getOrNull(index)
            ?: throw CommandParseException("§cError: Please specify a player name.")

        playerManager.getPlayer(targetName)?.let { online ->
            lookupService.record(online.uniqueId, online.name)
            return OfflinePlayer(online.uniqueId, online.name)
        }

        val uuid = lookupService.resolveUuid(targetName)
            ?: throw CommandParseException("§cError: '$targetName' has never joined this server.")

        return OfflinePlayer(uuid, lookupService.resolveName(uuid) ?: targetName)
    }

    override fun getSuggestions(sender: CommandSender, args: Array<String>, index: Int): List<String> {
        val partial = args.getOrNull(index)?.lowercase() ?: ""
        return playerManager.getOnlinePlayers()
            .map { it.name }
            .filter { it.lowercase().startsWith(partial) }
    }
}
