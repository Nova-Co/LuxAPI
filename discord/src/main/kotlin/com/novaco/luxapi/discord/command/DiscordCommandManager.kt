package com.novaco.luxapi.discord.command

import com.novaco.luxapi.commons.command.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Registers [com.novaco.luxapi.discord.command.annotation.DiscordCommand]-annotated
 * instances and routes incoming slash command interactions to them.
 */
class DiscordCommandManager : CommandManager {

    private companion object {
        val logger = LoggerFactory.getLogger(DiscordCommandManager::class.java)
    }

    val registeredCommands = ConcurrentHashMap<String, DiscordCommandProcessor>()

    /**
     * Wraps [commandInstance] in a [DiscordCommandProcessor] and registers it by its
     * lowercase command name.
     */
    override fun register(commandInstance: Any) {
        val processor = DiscordCommandProcessor(commandInstance)
        registeredCommands[processor.commandInfo.name.lowercase()] = processor
    }

    /**
     * Routes an incoming slash command interaction to its registered processor.
     * Logs a warning and returns instead of throwing if no command with that name is
     * registered (e.g. a command was removed from code but not yet un-registered with
     * Discord).
     */
    fun dispatch(event: SlashCommandInteractionEvent) {
        val processor = registeredCommands[event.name.lowercase()]
        if (processor == null) {
            logger.warn("Received interaction for unregistered Discord command '{}'", event.name)
            return
        }
        processor.handle(event)
    }

    /**
     * Pushes every registered command's [DiscordCommandProcessor.toCommandData] to
     * Discord. Guild-scoped updates (`guildId != 0`) propagate near-instantly and are
     * recommended during development; global updates (`guildId == 0`) can take up to
     * an hour to propagate to all guilds.
     *
     * @param jda The connected JDA instance.
     * @param guildId The guild to scope registration to, or `0` for global registration.
     */
    fun updateCommands(jda: JDA, guildId: Long) {
        val commandData = registeredCommands.values.map { it.toCommandData() }

        if (guildId == 0L) {
            jda.updateCommands().addCommands(commandData).queue()
        } else {
            val guild = jda.getGuildById(guildId)
            if (guild == null) {
                logger.warn("Cannot register Discord commands: guild '{}' is not visible to this bot.", guildId)
                return
            }
            guild.updateCommands().addCommands(commandData).queue()
        }
    }
}
